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

        // Get suggested properties for the entity message
        ChatClientSuggestedProperties messageProperties = super.getSuggestedChatClientProperties(aiMessage.getMessage());

        log.debug("Suggested properties for entity message: {}", messageProperties);

        // Update the system prompt to encourage specific actionable responses
        String updatedPrompt = "Please provide specific destinations and actionable suggestions relevant to the user's query.";

        // Build the DTO for processing
        MessageProcessingStrategyDTO messageProcessingStrategyDTO = MessageProcessingStrategyDTO
                .builder()
                .aiMessage(aiMessage)
                .client(getChatClient())
                .requiredFunctions(messageProperties.getFunctionNames())
                .systemPrompt(new Prompt(updatedPrompt))
                .returnClassType(returnClassType)
                .build();

        // Process the message with EntityReturnProcessingStrategy
        EntityReturnProcessingStrategy<T> entityReturnProcessingStrategy = new EntityReturnProcessingStrategy<>();
        T result = entityReturnProcessingStrategy.processMessage(messageProcessingStrategyDTO);

        log.info("Processed entity message result: {}", result);

        // If the result is an AIMessageResponse, sanitize and refine suggestions
        if (result instanceof AIMessageResponse) {
            AIMessageResponse response = (AIMessageResponse) result;

            // Generate actionable suggestions
            List<String> actionableSuggestions = generateSuggestions(response.getText());

            // Update the response with actionable suggestions
            response.setSuggestions(actionableSuggestions);
        }

        return result;
    }

    // Helper method to generate suggestions based on the context
    private List<String> generateSuggestions(String context) {
        if (context.toLowerCase().contains("beach")) {
            return List.of("Discover hidden beach spots", "Find top-rated beach resorts", "Check snorkeling options", "Plan a beach adventure");
        } else if (context.toLowerCase().contains("city")) {
            return List.of("Explore city landmarks", "Find local events", "Discover restaurants", "Check guided tours");
        } else if (context.toLowerCase().contains("cuisine")) {
            return List.of("Find local restaurants", "Explore food tours", "Discover regional dishes", "Learn about local food history");
        }
        // Default suggestions
        return List.of("Browse destinations", "Check activities", "Explore travel packages", "Plan your trip");
    }

    @Override
    public boolean supports(AIMessageDTO inputMessage) {
        // Check if the agent supports the given message
        return inputMessage != null && inputMessage.getMessage() != null
                && Objects.isNull(inputMessage.getMedia());
    }
}






