package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.ChatClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ChatStreamService extends AbstractChatService {

    public ChatStreamService(ChatClientFactory chatClientFactory, List<String> aiFunctionDescriptions) {
        super(chatClientFactory, aiFunctionDescriptions);
        log.info("ChatCallService initialized with AI function descriptions: {}", aiFunctionDescriptions);
    }

    public String processMessage(AIMessageDTO message) {
        log.info("Processing message: {}", message.getMessage());

        List<FunctionNameSuggestion> requiredFunctions = getRequiredFunctions(message.getMessage());

        if (requiredFunctions.isEmpty()) {
            log.info("No required functions found, asking question without functions.");
            return askQuestion(message);
        } else {
            log.info("Required functions found, asking question with functions: {}", requiredFunctions);
            return askQuestionWithFunctions(message, requiredFunctions);
        }
    }

    private String askQuestion(AIMessageDTO message) {
        log.debug("Asking question without functions for message: {}", message.getMessage());
        return getChatClient().prompt()
                .system(singleQuestionPrompt)
                .user(message.getMessage())
                .call()
                .content();
    }

    private String askQuestionWithFunctions(AIMessageDTO message, List<FunctionNameSuggestion> functions) {
        log.debug("Asking question with functions for message: {}, functions: {}", message.getMessage(), functions);
        return getChatClient().prompt()
                .system(withFunctionsQuestionPrompt)
                .user(message.getMessage())
                .functions(functionNames(functions))
                .call()
                .content();
    }
}