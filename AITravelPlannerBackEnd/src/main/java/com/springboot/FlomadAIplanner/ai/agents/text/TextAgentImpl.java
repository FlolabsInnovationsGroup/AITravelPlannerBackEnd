package com.springboot.FlomadAIplanner.ai.agents.text;

import com.springboot.FlomadAIplanner.ai.agents.messageProcessing.EntityReturnProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.agents.messageProcessing.MessageProcessingStrategy;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.ChatClientSuggestedProperties;
import com.springboot.FlomadAIplanner.ai.dto.MessageProcessingStrategyDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;

/**
 * Represents an agent that wraps a ChatClient with additional configurations and message processing strategies.
 */
@Slf4j
public class TextAgentImpl<S, F> extends AbstractTextAgent<S, F> {

    public TextAgentImpl(ChatClient chatClient, List<String> permittedFunctions,
                         MessageProcessingStrategy<S> callMessageProcessingStrategy,
                         MessageProcessingStrategy<F> streamMessageProcessingStrategy,
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
                .agent(this)
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
                .agent(this)
                .requiredFunctions(messageProperties.getFunctionNames())
                .systemPrompt(new Prompt(messageProperties.getSystemPrompt()))
                .build();

        return getStreamMessageProcessingStrategy().processMessage(messageProcessingStrategyDTO);
    }

    @Override
    public <T> T processEntityMessage(AIMessageDTO aiMessage, Class<T> returnClassType) {
        log.debug("Processing entity message: {}", aiMessage.getMessage());
        ChatClientSuggestedProperties messageProperties = super.getSuggestedChatClientProperties(aiMessage.getMessage());

        log.debug("Suggested properties for entity message: {}", messageProperties);
        MessageProcessingStrategyDTO messageProcessingStrategyDTO = MessageProcessingStrategyDTO
                .builder()
                .aiMessage(aiMessage)
                .agent(this)
                .requiredFunctions(messageProperties.getFunctionNames())
                .systemPrompt(new Prompt(messageProperties.getSystemPrompt()))
                .returnClassType(returnClassType)
                .build();

        EntityReturnProcessingStrategy<T> entityReturnProcessingStrategy = new EntityReturnProcessingStrategy<>();
        T result = entityReturnProcessingStrategy.processMessage(messageProcessingStrategyDTO);
        log.info("Processed entity message result: {}", StringUtils.substring(result.toString(), 0, 50));
        return result;
    }
}