package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.ChatClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
     * Processes the incoming message by determining required functions and querying the AI model.
     *
     * @param message the incoming message DTO
     * @return the response from the AI model
     */
    public String processMessage(AIMessageDTO message) {
        log.info("Processing message: {}", message.getMessage());

        ChatClientSuggestedProperties properties = getRequiredFunctions(message.getMessage());

        if (properties.getFunctionNames().isEmpty()) {
            log.info("No required functions found, asking question without functions.");
            return askQuestion(message);
        } else {
            log.info("Required functions found, asking question with functions: {}", properties.getFunctionNames());
            return askQuestionWithFunctions(properties.getSystemPrompt(), message, properties.getFunctionNames());
        }
    }

    /**
     * Asks a question to the AI model without specifying any functions.
     *
     * @param message the incoming message DTO
     * @return the response from the AI model
     */
    private String askQuestion(AIMessageDTO message) {
        log.debug("Asking question without functions for message: {}", message.getMessage());
        return getChatClient().prompt()
                .system(promptsList.get("singleQuestionPrompt.txt"))
                .user(message.getMessage())
                .call()
                .content();
    }
    
    /**
     * Asks a question to the AI model specifying the required functions.
     *
     * @param message   the incoming message DTO
     * @param functions the list of required functions
     * @return the response from the AI model
     */
    private String askQuestionWithFunctions(String systemPrompt, AIMessageDTO message, List<String> functionNames) {
        log.debug("Asking question with functions for message: {}, functions: {}", message.getMessage(), functionNames);
        return getChatClient().prompt()
                .system(systemPrompt)
                .user(message.getMessage())
                .functions(functionNames.toArray(new String[0]))
                .call()
                .content();
    }
}