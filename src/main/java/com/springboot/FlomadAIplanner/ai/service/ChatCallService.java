package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.ChatClientSuggestedProperties;
import com.springboot.FlomadAIplanner.ai.factory.ChatClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

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
     * Processes a aiMessage and determines the appropriate response.
     *
     * @param aiMessage The user aiMessage DTO
     * @return The response content
     */
    public String processMessage(AIMessageDTO aiMessage) {
        log.info("Processing aiMessage: {}; sessionId: {}", aiMessage.getMessage(), aiMessage.getSessionId());
        ChatClientSuggestedProperties properties = getSuggestedChatClientProperties(aiMessage.getMessage());

        return askQuestionWithParameters(properties, aiMessage);
    }

    /**
     * Retrieves the suggested chat client properties based on the aiMessage content.
     *
     * @param clientProperties The chat client suggested properties
     * @param aiMessage        The user aiMessage
     * @return processed response for user from LLM
     */
    private String askQuestionWithParameters(ChatClientSuggestedProperties clientProperties, AIMessageDTO aiMessage) {
        List<String> functions = clientProperties.getFunctionNames();
        String systemPrompt = clientProperties.getSystemPrompt();

        log.debug("Asking question with parameters for aiMessage: {}, functions: {}", aiMessage.getMessage(), functions);

        return getChatClient().prompt()
                .system(systemPrompt)
                .user(aiMessage.getMessage())
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, aiMessage.getSessionId()))
                .functions(functions.toArray(new String[0]))
                .call()
                .content();
    }

}