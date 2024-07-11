package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.ChatClientSuggestedProperties;
import com.springboot.FlomadAIplanner.ai.factory.ChatClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ChatCallService extends AbstractChatService to provide specific functionality for processing chat messages.
 * This service analyzes the message content to determine required functions and processes the message accordingly.
 */
@Slf4j
@Service
public class ChatCallService extends AbstractChatService {

    /**
     * Constructor for ChatCallService.
     *
     * @param chatClientFactory      Factory for creating chat clients
     * @param aiFunctionDescriptions List of available AI function descriptions
     */
    public ChatCallService(ChatClientFactory chatClientFactory, List<String> aiFunctionDescriptions, Map<String, String> promptsList) {
        super(chatClientFactory, aiFunctionDescriptions, promptsList);
        log.info("ChatCallService initialized with AI function descriptions: {}", aiFunctionDescriptions);
    }

    /**
     * Processes a message and determines the appropriate response.
     *
     * @param message The user message DTO
     * @return The response content
     */
    public String processMessage(AIMessageDTO message) {
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
    private String askQuestionWithParameters(ChatClientSuggestedProperties clientProperties, AIMessageDTO message) {
        List<String> functions = clientProperties.getFunctionNames();
        String systemPrompt = clientProperties.getSystemPrompt();

        log.debug("Asking question with parameters for message: {}, functions: {}", message.getMessage(), functions);

        return getChatClient().prompt()
                .system(systemPrompt)
                .user(message.getMessage())
                .functions(functions.toArray(new String[0]))
                .call()
                .content();
    }

}
