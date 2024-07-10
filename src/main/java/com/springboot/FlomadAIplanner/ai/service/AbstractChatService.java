package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.config.AIConfig;
import com.springboot.FlomadAIplanner.ai.dto.ChatClientSuggestedProperties;
import com.springboot.FlomadAIplanner.ai.factory.ChatClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    // Prompts stored as a map
    protected final Map<String, String> promptsList;
    @Value("classpath:/templates/PromptAndFunctionSuggestionsPrompt.txt")
    protected Resource promptAndFunctionSuggestionsPrompt;

    /**
     * Constructor for AbstractChatService.
     *
     * @param chatClientFactory      Factory for creating chat clients
     * @param aiFunctionDescriptions List of available AI function descriptions
     * @param promptsList            Map of prompt file names and their content
     */
    public AbstractChatService(ChatClientFactory chatClientFactory, List<String> aiFunctionDescriptions,
                               @Qualifier("templatePrompts") Map<String, String> promptsList) {
        this.chatClientFactory = chatClientFactory;
        this.aiFunctionDescriptions = Collections.unmodifiableList(aiFunctionDescriptions);
        this.promptsList = promptsList;
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
     * @return ChatClientSuggestedProperties
     */
    protected ChatClientSuggestedProperties getSuggestedChatClientProperties(String message) {
        log.debug("Getting required properties for message: {}", message);

        ChatClientSuggestedProperties properties = getChatClient().prompt()
                .user(u -> u.text(promptAndFunctionSuggestionsPrompt)
                        .param("promptsList", promptsList.toString())
                        .param("functionsList", aiFunctionDescriptions.toString())
                        .param("message", message))
                .advisors(a -> a.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 0))
                .call()
                .entity(ChatClientSuggestedProperties.class);

        log.info("promptKey: {}", properties.getSystemPrompt());

        // setting the suggested prompt content
        String suggestedPrompt = promptsList.get(properties.getSystemPrompt());
        properties.setSystemPrompt(suggestedPrompt);

        log.info("""
                Required properties received from AI suggestion:
                Chosen prompt: {};
                Chosen functions: {}""", suggestedPrompt, properties.getFunctionNames());
        return properties;
    }

}
