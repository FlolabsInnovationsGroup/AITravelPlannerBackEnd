package com.springboot.FlomadAIplanner.ai.agents.text;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageResponse;

import com.springboot.FlomadAIplanner.ai.agents.MessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.agents.text.strategies.EntityReturnProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.ChatClientSuggestedProperties;
import com.springboot.FlomadAIplanner.ai.dto.MessageProcessingStrategyDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
public class TextAgentImpl<S, F extends Flux<?>> extends AbstractTextAgent<S, F> implements Agent {

    public TextAgentImpl(ChatClient chatClient, List<String> permittedFunctions,
                         MessageProcessingStrategy<MessageProcessingStrategyDTO, S> callMessageProcessingStrategy,
                         MessageProcessingStrategy<MessageProcessingStrategyDTO, F> streamMessageProcessingStrategy,
                         Map<String, String> promptsList) {
        super(chatClient, permittedFunctions, callMessageProcessingStrategy, streamMessageProcessingStrategy, promptsList);
    }

    @Override
    public S processCallMessage(AIMessageDTO aiMessage) {
        log.debug("Processing call message: {}", aiMessage.getMessage());
        ChatClientSuggestedProperties messageProperties = super.getSuggestedChatClientProperties(aiMessage.getMessage());

        log.debug("Suggested properties for call message: {}", messageProperties);
        MessageProcessingStrategyDTO messageProcessingStrategyDTO = MessageProcessingStrategyDTO
                .builder()
                .aiMessage(aiMessage)
                .client(getChatClient())
                .requiredFunctions(messageProperties.getFunctionNames())
                .systemPrompt(new Prompt(messageProperties.getSystemPrompt()))
                .build();

        S result = getCallMessageProcessingStrategy().processMessage(messageProcessingStrategyDTO);
        log.info("Processed call message result: {}", StringUtils.substring(result.toString(), 0, 50));
        return result;
    }

    @Override
    public F processStreamMessage(AIMessageDTO aiMessage) {
        log.debug("Processing stream message: {}", aiMessage.getMessage());
        ChatClientSuggestedProperties messageProperties = super.getSuggestedChatClientProperties(aiMessage.getMessage());

        log.debug("Suggested properties for stream message: {}", messageProperties);
        MessageProcessingStrategyDTO messageProcessingStrategyDTO = MessageProcessingStrategyDTO
                .builder()
                .aiMessage(aiMessage)
                .client(getChatClient())
                .requiredFunctions(messageProperties.getFunctionNames())
                .systemPrompt(new Prompt(messageProperties.getSystemPrompt()))
                .build();

        return getStreamMessageProcessingStrategy().processMessage(messageProcessingStrategyDTO);
    }

    @Override
    public <T> T processEntityMessage(AIMessageDTO aiMessage, Class<T> returnClassType) {
        log.debug("Processing entity message: {}", aiMessage.getMessage());

        // Assuming getSuggestedChatClientProperties returns an object with function names and system prompt
        ChatClientSuggestedProperties messageProperties = super.getSuggestedChatClientProperties(aiMessage.getMessage());

        log.debug("Suggested properties for entity message: {}", messageProperties);

        // Update the system prompt to ensure actionable suggestions
        String updatedPrompt = "Please provide actionable suggestions like 'Browse destinations', 'Check activities', 'Explore travel dates'.";

        // Create the MessageProcessingStrategyDTO with the updated system prompt
        MessageProcessingStrategyDTO messageProcessingStrategyDTO = MessageProcessingStrategyDTO
                .builder()
                .aiMessage(aiMessage)
                .client(getChatClient())
                .requiredFunctions(messageProperties.getFunctionNames())
                .systemPrompt(new Prompt(updatedPrompt)) // Updated prompt
                .returnClassType(returnClassType)
                .build();

        // Use the EntityReturnProcessingStrategy to process the message and return a result
        EntityReturnProcessingStrategy<T> entityReturnProcessingStrategy = new EntityReturnProcessingStrategy<>();
        T result = entityReturnProcessingStrategy.processMessage(messageProcessingStrategyDTO);

        log.info("Processed entity message result: {}", result);

        // Adding the action suggestions back to the result (if needed)
        if (result instanceof AIMessageResponse) {
            AIMessageResponse response = (AIMessageResponse) result;

            // Sanitize suggestions: Convert questions into actionable statements if needed
            List<String> actionableSuggestions = sanitizeSuggestions(response.getSuggestions());

            // Set the sanitized actionable suggestions
            response.setSuggestions(actionableSuggestions);
        }

        return result;
    }

    // Helper method to sanitize suggestions (convert questions to actionable items)
    private List<String> sanitizeSuggestions(List<String> suggestions) {
        return suggestions.stream()
                .map(suggestion -> {
                    // Clean up any leading/trailing spaces and remove question marks
                    String sanitized = suggestion.trim().replaceAll("[?]$", "");

                    // Convert question-based suggestions to actionable suggestions
                    if (sanitized.toLowerCase().contains("where")) {
                        return "Browse " + sanitized.replaceAll("(?i)where", "").trim() + ".";  // Example: "Browse destinations."
                    } else if (sanitized.toLowerCase().contains("what")) {
                        return "Check " + sanitized.replaceAll("(?i)what", "").trim() + ".";  // Example: "Check activities."
                    } else if (sanitized.toLowerCase().contains("how")) {
                        return "Explore " + sanitized.replaceAll("(?i)how", "").trim() + ".";  // Example: "Explore travel dates."
                    } else if (sanitized.toLowerCase().contains("why")) {
                        return "Understand " + sanitized.replaceAll("(?i)why", "").trim() + ".";  // Example: "Understand travel recommendations."
                    } else if (sanitized.toLowerCase().contains("when")) {
                        return "Find out when " + sanitized.replaceAll("(?i)when", "").trim() + ".";  // Example: "Find out when to travel."
                    } else {
                        // Default case for non-question suggestions, add period for consistency
                        return sanitized + ".";  // Adding period for consistency
                    }
                })
                .collect(Collectors.toList());
    }















    @Override
    public boolean supports(AIMessageDTO inputMessage) {
        // Implement the logic to determine if the agent supports the given message
        return inputMessage != null && inputMessage.getMessage() != null
                && Objects.isNull(inputMessage.getMedia());
    }
}



