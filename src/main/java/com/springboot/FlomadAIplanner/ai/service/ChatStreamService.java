package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.ChatClientFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class ChatStreamService extends AbstractChatService {

    public ChatStreamService(ChatClientFactory chatClientFactory, List<String> aiFunctionDescriptions) {
        super(chatClientFactory, aiFunctionDescriptions);
    }

    public Flux<String> processMessage(AIMessageDTO message) {
        List<FunctionNameSuggestion> requiredFunctions = getRequiredFunctions(message.getMessage());

        if (requiredFunctions.isEmpty()) {
            return askQuestion(message);
        } else {
            return askQuestionWithFunctions(message, requiredFunctions);
        }
    }

    private Flux<String> askQuestion(AIMessageDTO message) {
        return getChatClient().prompt()
                .system(singleQuestionPrompt)
                .user(message.getMessage())
                .stream()
                .content();
    }

    private Flux<String> askQuestionWithFunctions(AIMessageDTO message, List<FunctionNameSuggestion> functions) {
        return getChatClient().prompt()
                .system(withFunctionsQuestionPrompt)
                .user(message.getMessage())
                .functions(functionNames(functions))
                .stream()
                .content();
    }
}
