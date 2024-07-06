package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.config.AIConfig;
import com.springboot.FlomadAIplanner.ai.factory.ChatClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Slf4j
public abstract class AbstractChatService {

    protected final ChatClientFactory chatClientFactory;
    protected final List<String> aiFunctionDescriptions;
    @Value("classpath:/templates/FunctionsSuggestionsPrompt.txt")
    protected Resource functionSuggestionsPrompt;
    @Value("classpath:/templates/SingleQuestionPrompt.txt")
    protected Resource singleQuestionPrompt;
    @Value("classpath:/templates/WithFunctionsQuestionPrompt.txt")
    protected Resource withFunctionsQuestionPrompt;

    public AbstractChatService(ChatClientFactory chatClientFactory, List<String> aiFunctionDescriptions) {
        this.chatClientFactory = chatClientFactory;
        this.aiFunctionDescriptions = Collections.unmodifiableList(aiFunctionDescriptions);
        log.info("Chat service initialized with AI function descriptions: {}", aiFunctionDescriptions);
    }

    protected ChatClient getChatClient() {
        log.debug("Getting chat client with required AI model.");
        return chatClientFactory.getClient(getRequiredAIModel());
    }

    private String getRequiredAIModel() {
        String model = AIConfig.OPENAI_MODEL;  // Consider injecting this dependency
        log.debug("Using AI model: {}", model);
        return model;
    }

    protected List<FunctionNameSuggestion> getRequiredFunctions(String message) {
        log.debug("Getting required functions for message: {}", message);
        List<FunctionNameSuggestion> functions = getChatClient().prompt()
                .user(u -> u.text(functionSuggestionsPrompt)
                        .param("functionsList", aiFunctionDescriptions.toString())
                        .param("message", message))
                .advisors(a -> a
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 0))
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
        log.debug("Required functions found: {}", functions);
        return functions;
    }

    protected String[] functionNames(List<FunctionNameSuggestion> functions) {
        log.debug("Extracting function names from functions: {}", functions);
        String[] functionNames = functions.stream()
                .map(FunctionNameSuggestion::functionName)
                .toArray(String[]::new);
        log.debug("Extracted function names: {}", Arrays.toString(functionNames));
        return functionNames;
    }
}
