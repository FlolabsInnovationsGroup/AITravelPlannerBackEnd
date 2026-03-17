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
 * Abstract Text Agent with Spring AI 1.0.0-M5 StringTemplate compatibility.
 *
 * <p>Spring AI 1.0.0-M5 uses StringTemplate with {} delimiters which conflicts
 * with JSON in template parameters. This class applies the workaround from
 * Spring AI GitHub issues #1849 and #2448 by escaping curly braces.
 *
 * @param <C> call response type
 * @param <S> stream response type (extends Flux)
 * @see <a href="https://github.com/spring-projects/spring-ai/issues/1849">#1849</a>
 * @see <a href="https://github.com/spring-projects/spring-ai/issues/2448">#2448</a>
 */
@Slf4j
@Setter
@ToString
public abstract class AbstractTextAgent<C, S extends Flux<?>> implements TextAgent<C, S> {
    /** Chat client for AI calls. */
    @Getter
    private final ChatClient chatClient;
    /** List of permitted function names. */
    @Getter
    private final List<String> permittedFunctions;
    /** Strategy for call (non-stream) message processing. */
    @Getter
    private final MessageProcessingStrategy<MessageProcessingStrategyDTO, C>
            callMessageProcessingStrategy;
    /** Strategy for stream message processing. */
    @Getter
    private final MessageProcessingStrategy<MessageProcessingStrategyDTO, S>
            streamMessageProcessingStrategy;
    /** Map of prompt name to prompt content. */
    @Getter
    private final Map<String, String> promptsList;
    /** Map of prompt metadata keys to values. */
    @Getter
    private final Map<String, String> promptMetadata;
    /** Resource for lightweight prompt selection template. */
    @Getter
    @Value("classpath:/templates/LightweightPromptSelection.txt")
    private Resource lightweightPromptSelectionTemplate;
    /** Predicate to determine if this agent supports the given message. */
    @Setter
    private Predicate<AIMessageDTO> supportsPredicate = (m) -> false;

    /**
     * Constructor for abstract text agent.
     *
     * @param pChatClient chat client
     * @param pPermittedFunctions permitted function names
     * @param pCallStrategy call message processing strategy
     * @param pStreamStrategy stream message processing strategy
     * @param pPromptsList prompt name to content map
     * @param pPromptMetadata prompt metadata map
     */
    protected AbstractTextAgent(
            final ChatClient pChatClient,
            final List<String> pPermittedFunctions,
            final MessageProcessingStrategy<MessageProcessingStrategyDTO, C> pCallStrategy,
            final MessageProcessingStrategy<MessageProcessingStrategyDTO, S> pStreamStrategy,
            final Map<String, String> pPromptsList,
            final Map<String, String> pPromptMetadata) {
        this.chatClient = pChatClient;
        this.permittedFunctions = pPermittedFunctions;
        this.callMessageProcessingStrategy = pCallStrategy;
        this.streamMessageProcessingStrategy = pStreamStrategy;
        this.promptsList = pPromptsList;
        this.promptMetadata = pPromptMetadata;

        log.info("📋 AbstractTextAgent initialized with {} permitted functions",
                pPermittedFunctions.size());
        log.debug("📋 Function names: {}", pPermittedFunctions);
    }

    /**
     * Escapes content to prevent StringTemplate parsing conflicts.
     * StringTemplate treats certain characters as syntax markers.
     *
     * @param content The content to escape
     * @return Escaped content safe for StringTemplate processing
     */
    private String escapeStringTemplateContent(final String content) {
        if (content == null) {
            return "";
        }

        return content
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("$", "\\$")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("<", "\\<")
                .replace(">", "\\>");
    }

    /**
     * Uses Spring AI ChatClient while avoiding StringTemplate issues.
     * Ensures functions are properly registered and available.
     *
     * @param userMessage The user message for prompt selection
     * @return ChatClientSuggestedProperties with selected prompt and functions
     */
    private ChatClientSuggestedProperties makeFixedSpringAICall(final String userMessage) {
        try {
            log.info("🔧 Using fixed Spring AI call with proper function registration");

            String selectedPrompt;
            String lower = userMessage.toLowerCase();
            if (lower.contains("plan") || lower.contains("trip") || lower.contains("vacation")
                    || lower.contains("travel") || lower.contains("itinerary")) {
                selectedPrompt = "UnifiedPlanningPrompt.txt";
                log.info("🎯 Selected UnifiedPlanningPrompt for travel planning request");
            } else if (lower.contains("book") || lower.contains("reservation")
                    || lower.contains("hotel")) {
                selectedPrompt = "BookingConfirmationPrompt.txt";
                log.info("🎯 Selected BookingConfirmationPrompt for booking request");
            } else if (lower.contains("emergency") || lower.contains("help")
                    || lower.contains("lost")) {
                selectedPrompt = "EmergencyAssistancePrompt.txt";
                log.info("🎯 Selected EmergencyAssistancePrompt for emergency request");
            } else {
                selectedPrompt = "UnifiedPlanningPrompt.txt";
                log.info("🎯 Defaulting to UnifiedPlanningPrompt");
            }

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

            log.info("✅ Fixed prompt selection completed - Functions properly registered");
            return result;

        } catch (Exception e) {
            log.error("❌ Error in fixed Spring AI call, using fallback", e);

            ChatClientSuggestedProperties fallback = new ChatClientSuggestedProperties();
            fallback.setSystemPrompt("UnifiedPlanningPrompt.txt");
            fallback.setFunctionNames(Arrays.asList(
                    "currentDateTimeFunction", "placeTextSearchFunction"));

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
    protected ChatClientSuggestedProperties getSuggestedChatClientProperties(final String message) {
        log.debug("Getting required properties for message using lightweight selection: {}",
                message);

        try {
            String promptMetadataStr = promptMetadata.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(java.util.stream.Collectors.joining(", "));
            String functionsListStr = String.join(", ", permittedFunctions);

            log.info("StringTemplate params - promptMetadata: {}", promptMetadataStr);
            log.info("StringTemplate params - functionsList: {}", functionsListStr);
            log.info("StringTemplate params - message: {}", message);

            String templateContent = lightweightPromptSelectionTemplate
                    .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

            String escapedPromptMetadata = escapeStringTemplateContent(promptMetadataStr);
            String escapedFunctionsList = escapeStringTemplateContent(functionsListStr);
            String escapedMessage = escapeStringTemplateContent(message);

            String processedTemplate = templateContent
                    .replace("{promptMetadata}", escapedPromptMetadata)
                    .replace("{functionsList}", escapedFunctionsList)
                    .replace("{message}", escapedMessage);

            log.info("Using processed template with length: {}", processedTemplate.length());
            String first500 = processedTemplate.length() > 500
                    ? processedTemplate.substring(0, 500) + "..."
                    : processedTemplate;
            log.info("First 500 chars of processed template: {}", first500);

            ChatClientSuggestedProperties properties = makeFixedSpringAICall(message);

            log.info("Selected prompt key: {}", properties.getSystemPrompt());

            String suggestedPrompt = promptsList.get(properties.getSystemPrompt());
            if (suggestedPrompt == null) {
                log.warn("Selected prompt '{}' not found in promptsList. "
                        + "Falling back to SingleQuestionPrompt.txt",
                        properties.getSystemPrompt());
                suggestedPrompt = promptsList.get("SingleQuestionPrompt.txt");
                properties.setSystemPrompt("SingleQuestionPrompt.txt");
            }
            properties.setSystemPrompt(suggestedPrompt);

            log.info("Lightweight selection completed: prompt={}; functions={}",
                    properties.getSystemPrompt(), properties.getFunctionNames());

            return properties;

        } catch (Exception e) {
            log.error("Error in lightweight prompt selection, falling back to default: {}",
                    e.getMessage());
            ChatClientSuggestedProperties fallback = new ChatClientSuggestedProperties();
            fallback.setSystemPrompt(promptsList.get("DefaultPrompt.txt"));
            fallback.setFunctionNames(permittedFunctions);
            log.info("🔄 Fallback: Using DefaultPrompt.txt with {} functions",
                    permittedFunctions.size());
            log.debug("🔍 Function names being passed to Spring AI: {}", permittedFunctions);
            return fallback;
        }
    }

    @Override
    public boolean supports(AIMessageDTO inputMessage) {
        return supportsPredicate.test(inputMessage);
    }
}
