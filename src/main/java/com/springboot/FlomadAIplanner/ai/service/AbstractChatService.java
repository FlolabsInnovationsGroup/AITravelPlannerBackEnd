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

/**
 * AbstractChatService serves as a base class for chat services, providing common functionality
 * for interacting with the chat client, retrieving function suggestions, and processing prompts.
 */
@Slf4j
public abstract class AbstractChatService {

    // Factory for creating chat client instances
    protected final ChatClientFactory chatClientFactory;

    // List of AI function descriptions available for use
    protected final List<String> aiFunctionDescriptions;

    // Prompts stored as resources
    @Value("classpath:/templates/FunctionsSuggestionsPrompt.txt")
    protected Resource functionSuggestionsPrompt;
    @Value("classpath:/templates/SingleQuestionPrompt.txt")
    protected Resource singleQuestionPrompt;
    @Value("classpath:/templates/promptAndFunctionSuggestionsPrompt.txt")
    protected Resource promptAndFunctionSuggestionsPrompt;
    @Value("classpath:/templates/WithFunctionsQuestionPrompt.txt")
    protected Resource withFunctionsQuestionPrompt;
    @Value("classpath:/templates/DefaultRulesPrompt.txt")
    protected Resource defaultRulesPrompt;
    @Value("classpath:/templates/InternetSearchRulesPrompt.txt")
    protected Resource internetSearchRulesPrompt;
    @Value("classpath:/templates/FallbackPrompt.txt")
    protected Resource fallbackPrompt;

    /**
     * Constructor for AbstractChatService.
     * 
     * @param chatClientFactory Factory for creating chat clients
     * @param aiFunctionDescriptions List of available AI function descriptions
     */
    public AbstractChatService(ChatClientFactory chatClientFactory, List<String> aiFunctionDescriptions) {
        this.chatClientFactory = chatClientFactory;
        this.aiFunctionDescriptions = Collections.unmodifiableList(aiFunctionDescriptions);
        log.info("Chat service initialized with AI function descriptions: {}", aiFunctionDescriptions);
    }

    /**
     * Retrieves the appropriate ChatClient based on the required AI model.
     * 
     * @return An instance of ChatClient
     */
    protected ChatClient getChatClient() {
        log.debug("Getting chat client with required AI model.");
        return chatClientFactory.getClient(getRequiredAIModel());
    }

    /**
     * Determines the required AI model for the chat client.
     * 
     * @return The name of the required AI model
     */
    private String getRequiredAIModel() {
        String model = AIConfig.OPENAI_MODEL;  // Consider injecting this dependency
        log.debug("Using AI model: {}", model);
        return model;
    }

    /**
     * Retrieves the required functions based on the given message.
     * 
     * @param message The user message
     * @return A list of function name suggestions
     */
    protected List<FunctionNameSuggestion> getRequiredFunctions(String message) {
        log.debug("Getting required functions for message: {}", message);
        List<FunctionNameSuggestion> functions = getChatClient().prompt()
                .system(promptAndFunctionSuggestionsPrompt) 
                .user(u -> u.text(functionSuggestionsPrompt)
                        .param("functionsList", aiFunctionDescriptions.toString())
                        .param("message", message))
                .advisors(a -> a
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 0))
                .call()
                .entity(new ParameterizedTypeReference<List<FunctionNameSuggestion>>() {
                });
                
        if (functions.isEmpty()) {
            log.debug("No specific functions found, applying default rules.");
            functions = getChatClient().prompt()
                    .system(defaultRulesPrompt)
                    .user(message)
                    .call()
                    .entity(new ParameterizedTypeReference<List<FunctionNameSuggestion>>() {
                    });
        
            if (functions.isEmpty()) {
                log.debug("No default rules found, applying fallback response.");
                functions = getChatClient().prompt()
                        .system(fallbackPrompt)
                        .user(message)
                        .call()
                        .entity(new ParameterizedTypeReference<List<FunctionNameSuggestion>>() {
                        });
            }
        }
        
        log.debug("Required functions found: {}", functions);
        return functions;
    }

    /**
     * Extracts function names from a list of function name suggestions.
     * 
     * @param functions List of function name suggestions
     * @return An array of function names
     */
    protected String[] functionNames(List<FunctionNameSuggestion> functions) {
        log.debug("Extracting function names from functions: {}", functions);
        String[] functionNames = functions.stream()
                .map(FunctionNameSuggestion::getFunctionNames)
                .toArray(String[]::new);
        log.debug("Extracted function names: {}", Arrays.toString(functionNames));
        return functionNames;
    }
}