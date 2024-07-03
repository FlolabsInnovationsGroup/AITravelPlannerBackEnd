package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.ChatClientFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatCallService extends AbstractChatService {

    public ChatCallService(ChatClientFactory chatClientFactory, List<String> aiFunctionDescriptions) {
        super(chatClientFactory, aiFunctionDescriptions);
    }

    public String processMessage(AIMessageDTO message) {
        List<FunctionNameSuggestion> requiredFunctions = getRequiredFunctions(message.getMessage());

        if (requiredFunctions.isEmpty()) {
            return askQuestion(message);
        } else {
            return askQuestionWithFunctions(message, requiredFunctions);
        }
    }

    private String askQuestion(AIMessageDTO message) {
        return getChatClient().prompt()
                .system(singleQuestionPrompt)
                .user(message.getMessage())
                .call()
                .content();
    }

    private String askQuestionWithFunctions(AIMessageDTO message, List<FunctionNameSuggestion> functions) {
        return getChatClient().prompt()
                .system(withFunctionsQuestionPrompt)
                .user(message.getMessage())
                .functions(functionNames(functions))
                .call()
                .content();
    }
}
