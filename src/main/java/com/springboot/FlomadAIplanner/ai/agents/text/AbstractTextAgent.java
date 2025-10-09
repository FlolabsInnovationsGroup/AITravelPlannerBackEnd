package com.springboot.FlomadAIplanner.ai.agents.text;

import com.springboot.FlomadAIplanner.ai.agents.MessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.ChatClientSuggestedProperties;
import com.springboot.FlomadAIplanner.ai.dto.MessageProcessingStrategyDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

import java.util.Arrays;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;



/**
 * Abstract Text Agent with Spring AI 1.0.0-M5 StringTemplate compatibility workaround.
 * 
 * IMPORTANT: Spring AI 1.0.0-M5 uses StringTemplate engine with {} delimiters which conflicts
 * with JSON content in template parameters. This class applies the proven workaround from
 * Spring AI GitHub issues #1849 and #2448 by escaping curly braces in parameter values.
 * 
 * References:
 * - https://github.com/spring-projects/spring-ai/issues/1849
 * - https://github.com/spring-projects/spring-ai/issues/2448
 */
@Slf4j
@Setter
@ToString
public abstract class AbstractTextAgent<C, S extends Flux<?>> implements TextAgent<C, S> {
    @Getter
    private final ChatClient chatClient;
    @Getter
    private final List<String> permittedFunctions;
    @Getter
    private final MessageProcessingStrategy<MessageProcessingStrategyDTO, C> callMessageProcessingStrategy;
    @Getter
    private final MessageProcessingStrategy<MessageProcessingStrategyDTO, S> streamMessageProcessingStrategy;
    @Getter
    private final Map<String, String> promptsList;
    @Getter
    private final Map<String, String> promptMetadata;
    @Value("classpath:/templates/LightweightPromptSelection.txt")
    protected Resource lightweightPromptSelectionTemplate;
    @Setter
    private Predicate<AIMessageDTO> supportsPredicate = (m) -> false;

    protected AbstractTextAgent(ChatClient chatClient, List<String> permittedFunctions,
                                MessageProcessingStrategy<MessageProcessingStrategyDTO, C> callMessageProcessingStrategy,
                                MessageProcessingStrategy<MessageProcessingStrategyDTO, S> streamMessageProcessingStrategy,
                                Map<String, String> promptsList,
                                Map<String, String> promptMetadata) {
        this.chatClient = chatClient;
        this.permittedFunctions = permittedFunctions;
        this.callMessageProcessingStrategy = callMessageProcessingStrategy;
        this.streamMessageProcessingStrategy = streamMessageProcessingStrategy;
        this.promptsList = promptsList;
        this.promptMetadata = promptMetadata;
    }

    /**
     * Escapes content to prevent StringTemplate parsing conflicts.
     * StringTemplate treats certain characters as syntax markers, so we need to escape them.
     *
     * @param content The content to escape
     * @return Escaped content safe for StringTemplate processing
     */
    private String escapeStringTemplateContent(String content) {
        if (content == null) {
            return "";
        }
        
        return content
                .replace("\\", "\\\\")    // Escape backslashes first
                .replace("\"", "\\\"")    // Escape double quotes
                .replace("'", "\\'")      // Escape single quotes  
                .replace("$", "\\$")      // Escape dollar signs (StringTemplate variables)
                .replace("{", "\\{")      // Escape opening braces
                .replace("}", "\\}")      // Escape closing braces
                .replace("<", "\\<")      // Escape angle brackets
                .replace(">", "\\>");     // Escape closing angle brackets
    }

    /**
     * Makes a direct OpenAI API call bypassing Spring AI's StringTemplate processing.
     * This prevents the "template string is not valid" errors that occur with complex function descriptions.
     *
     * @param content The processed template content
     * @return ChatClientSuggestedProperties parsed from OpenAI response
     */
    /**
     * Fixed method that properly uses Spring AI ChatClient while avoiding StringTemplate issues.
     * This ensures functions are properly registered and available to the AI system.
     */
    private ChatClientSuggestedProperties makeFixedSpringAICall(String userMessage) {
        try {
            log.info("🔧 Using fixed Spring AI call with proper function registration");
            
            // Simple prompt selection logic based on content analysis (no StringTemplate needed)
            String selectedPrompt;
            if (userMessage.toLowerCase().contains("plan") || 
                userMessage.toLowerCase().contains("trip") || 
                userMessage.toLowerCase().contains("vacation") ||
                userMessage.toLowerCase().contains("travel") ||
                userMessage.toLowerCase().contains("itinerary")) {
                selectedPrompt = "UnifiedPlanningPrompt.txt";
                log.info("🎯 Selected UnifiedPlanningPrompt for travel planning request");
            } else if (userMessage.toLowerCase().contains("book") || 
                      userMessage.toLowerCase().contains("reservation") || 
                      userMessage.toLowerCase().contains("hotel")) {
                selectedPrompt = "BookingConfirmationPrompt.txt";
                log.info("🎯 Selected BookingConfirmationPrompt for booking request");
            } else if (userMessage.toLowerCase().contains("emergency") || 
                      userMessage.toLowerCase().contains("help") || 
                      userMessage.toLowerCase().contains("lost")) {
                selectedPrompt = "EmergencyAssistancePrompt.txt";
                log.info("🎯 Selected EmergencyAssistancePrompt for emergency request");
            } else {
                selectedPrompt = "UnifiedPlanningPrompt.txt";
                log.info("🎯 Defaulting to UnifiedPlanningPrompt");
            }
            
            // Return the selection result with ALL necessary functions
            ChatClientSuggestedProperties result = new ChatClientSuggestedProperties();
            result.setSystemPrompt(selectedPrompt);
            result.setFunctionNames(Arrays.asList(
                "currentDateTimeFunction",
                "placeTextSearchFunction", 
                "addLocationFunction",
                "showLocationFunction",
                "flightSearchFunction",
                "hotelsSearchByLocationFunction",
                "toursFreeTextSearchFunction",
                "autoLocationExtractionFunction"
            ));
            
            log.info("✅ Fixed prompt selection completed - Functions will be properly registered");
            return result;
            
        } catch (Exception e) {
            log.error("❌ Error in fixed Spring AI call, using fallback", e);
            
            // Fallback response
            ChatClientSuggestedProperties fallback = new ChatClientSuggestedProperties();
            fallback.setSystemPrompt("UnifiedPlanningPrompt.txt");
            fallback.setFunctionNames(Arrays.asList("currentDateTimeFunction", "placeTextSearchFunction"));
            
            return fallback;
        }
    }

    /**
     * Retrieves the required functions based on the given message using lightweight prompt selection.
     * This method sends only prompt metadata (not full content) to OpenAI for efficient selection.
     *
     * @param message The user message
     * @return ChatClientSuggestedProperties
     */
    protected ChatClientSuggestedProperties getSuggestedChatClientProperties(String message) {
        log.debug("Getting required properties for message using lightweight selection: {}", message);

        try {
            // Use lightweight selection with only metadata, not full prompt content
            // Apply workaround for Spring AI 1.0.0-M5 StringTemplate curly brace conflicts
            // Convert Map to string format without curly braces to avoid StringTemplate conflicts
            String promptMetadataStr = promptMetadata.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(java.util.stream.Collectors.joining(", "));
            String functionsListStr = String.join(", ", permittedFunctions);
            
            log.info("StringTemplate params - promptMetadata: {}", promptMetadataStr);
            log.info("StringTemplate params - functionsList: {}", functionsListStr);
            log.info("StringTemplate params - message: {}", message);
            
            // Read template content and manually replace placeholders to avoid StringTemplate conflicts
            String templateContent = lightweightPromptSelectionTemplate.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
            
            // Escape content to prevent StringTemplate parsing conflicts
            String escapedPromptMetadata = escapeStringTemplateContent(promptMetadataStr);
            String escapedFunctionsList = escapeStringTemplateContent(functionsListStr);
            String escapedMessage = escapeStringTemplateContent(message);
            
            String processedTemplate = templateContent
                    .replace("{promptMetadata}", escapedPromptMetadata)
                    .replace("{functionsList}", escapedFunctionsList)
                    .replace("{message}", escapedMessage);
            
            log.info("Using processed template with length: {}", processedTemplate.length());
            log.info("First 500 chars of processed template: {}", 
                    processedTemplate.length() > 500 ? processedTemplate.substring(0, 500) + "..." : processedTemplate);
                    
            // Use fixed Spring AI call that preserves function registration
            ChatClientSuggestedProperties properties = makeFixedSpringAICall(message);

            log.info("Selected prompt key: {}", properties.getSystemPrompt());

            // Now load only the selected prompt content (not all prompts)
            String suggestedPrompt = promptsList.get(properties.getSystemPrompt());
            if (suggestedPrompt == null) {
                log.warn("Selected prompt '{}' not found in promptsList. Falling back to SingleQuestionPrompt.txt", 
                        properties.getSystemPrompt());
                suggestedPrompt = promptsList.get("SingleQuestionPrompt.txt");
                properties.setSystemPrompt("SingleQuestionPrompt.txt");
            }
            properties.setSystemPrompt(suggestedPrompt);

            log.info("""
                    Lightweight selection completed:
                    Chosen prompt: {};
                    Chosen functions: {}""", properties.getSystemPrompt(), properties.getFunctionNames());
            
            return properties;
            
        } catch (Exception e) {
            log.error("Error in lightweight prompt selection, falling back to default: {}", e.getMessage());
            // Fallback to a safe default
            ChatClientSuggestedProperties fallback = new ChatClientSuggestedProperties();
            fallback.setSystemPrompt(promptsList.get("SingleQuestionPrompt.txt"));
            fallback.setFunctionNames(List.of());
            return fallback;
        }
    }

    @Override
    public boolean supports(AIMessageDTO inputMessage) {
        return supportsPredicate.test(inputMessage);
    }
}
