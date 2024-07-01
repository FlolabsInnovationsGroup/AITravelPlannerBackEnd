package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.config.AIConfig;
import com.springboot.FlomadAIplanner.ai.factory.ChatClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;

import java.util.Collections;
import java.util.List;

@Slf4j
public abstract class AbstractChatService {

    @Value("classpath:/templates/FunctionsSuggestionsPrompt.txt")
    protected Resource functionSuggestionsPrompt;
    @Value("classpath:/templates/SingleQuestionPrompt.txt")
    protected Resource singleQuestionPrompt;
    @Value("classpath:/templates/WithFunctionsQuestionPrompt.txt")
    protected Resource withFunctionsQuestionPrompt;

    protected final ChatClientFactory chatClientFactory;
    protected final List<String> aiFunctionDescriptions;

    public AbstractChatService(ChatClientFactory chatClientFactory, List<String> aiFunctionDescriptions) {
        this.chatClientFactory = chatClientFactory;
        this.aiFunctionDescriptions = Collections.unmodifiableList(aiFunctionDescriptions);
        log.info("Chat service initialized with AI function descriptions: {}", aiFunctionDescriptions);
    }

    protected ChatClient getChatClient() {
        return chatClientFactory.getClient(getRequiredAIModel());
    }

    private String getRequiredAIModel() {
        return AIConfig.OPENAI_MODEL;  // Consider injecting this dependency
    }

    protected List<FunctionNameSuggestion> getRequiredFunctions(String message) {
        return getChatClient().prompt()
                .user(u -> u.text(functionSuggestionsPrompt)
                        .param("functionsList", aiFunctionDescriptions.toString())
                        .param("message", message))
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
    }

    protected String[] functionNames(List<FunctionNameSuggestion> functions) {
        return functions.stream()
                .map(FunctionNameSuggestion::functionName)
                .toArray(String[]::new);
    }
}
