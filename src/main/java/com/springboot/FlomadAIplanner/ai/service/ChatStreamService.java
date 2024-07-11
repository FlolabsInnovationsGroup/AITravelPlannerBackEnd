package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.ChatClientSuggestedProperties;
import com.springboot.FlomadAIplanner.ai.factory.ChatClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * ChatStreamService processes messages in a streaming manner.
 * It extends AbstractChatService to leverage common chat-related functionalities.
 */
@Slf4j
@Service
public class ChatStreamService extends AbstractChatService {

    /**
     * Constructs a new ChatStreamService.
     *
     * @param chatClientFactory      the factory to create ChatClient instances
     * @param aiFunctionDescriptions the list of AI function descriptions
     */
    public ChatStreamService(ChatClientFactory chatClientFactory, List<String> aiFunctionDescriptions, Map<String, String> promptsList) {
        super(chatClientFactory, aiFunctionDescriptions, promptsList);
        log.info("ChatCallService initialized with AI function descriptions: {}", aiFunctionDescriptions);
    }

    /**
     * Processes a message and determines the appropriate response.
     *
     * @param message The user message DTO
     * @return The response content
     */
    public Flux<String> processMessage(AIMessageDTO message) {
        log.info("Processing message: {}", message.getMessage());
        ChatClientSuggestedProperties properties = getSuggestedChatClientProperties(message.getMessage());

        return askQuestionWithParameters(properties, message);
    }

    /**
     * Retrieves the suggested chat client properties based on the message content.
     *
     * @param clientProperties The chat client suggested properties
     * @param message          The user message
     * @return processed response for user from LLM
     */
    private Flux<String> askQuestionWithParameters(ChatClientSuggestedProperties clientProperties, AIMessageDTO message) {
        List<String> functions = clientProperties.getFunctionNames();
        String systemPrompt = clientProperties.getSystemPrompt();

        log.debug("Asking question with parameters for message: {}, functions: {}", message.getMessage(), functions);

        return getChatClient().prompt()
                .system(systemPrompt)
                .user(message.getMessage())
                .functions(functions.toArray(new String[0]))
                .stream()
                .content();
    }

}