package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.agents.text.TextAgent;
import com.springboot.FlomadAIplanner.ai.config.cnst.AIModel;
import com.springboot.FlomadAIplanner.ai.factory.AgentFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * AbstractChatService serves as a base class for chat services, providing common functionality
 * for interacting with the chat client, retrieving function suggestions, and processing prompts.
 */
@Slf4j
@Getter
public abstract class AbstractChatService {

    // Factory for creating chat client instances
    private final AgentFactory agentFactory;
    // List of AI function descriptions available for use
    private final List<String> aiFunctionDescriptions;
    // Prompts stored as a map
    private final Map<String, String> promptsList;

    @Value("classpath:/templates/PromptAndFunctionSuggestionsPrompt.txt")
    @Setter
    private Resource promptAndFunctionSuggestionsPrompt;

    /**
     * Constructor for AbstractChatService.
     *
     * @param agentFactory           Factory for creating chat clients
     * @param aiFunctionDescriptions List of available AI function descriptions
     * @param promptsList            Map of prompt file names and their content
     */
    public AbstractChatService(AgentFactory agentFactory, List<String> aiFunctionDescriptions,
                               @Qualifier("templatePrompts") Map<String, String> promptsList) {
        this.agentFactory = agentFactory;
        this.aiFunctionDescriptions = Collections.unmodifiableList(aiFunctionDescriptions);
        this.promptsList = promptsList;
        log.info("Chat service initialized with AI function descriptions: {}", aiFunctionDescriptions);
    }

    /**
     * Retrieves the appropriate ChatClient based on the required AI model.
     *
     * @return An instance of ChatClient
     */
    protected TextAgent<?, ?> getChatAgent() {
        log.debug("Getting chat client with required AI model.");
        return agentFactory.getAgent(getRequiredAIModel());
    }

    /**
     * Determines the required AI model for the chat client.
     *
     * @return The name of the required AI model
     */
    private AIModel getRequiredAIModel() {
        AIModel model = AIModel.OPENAI_MODEL;  // Consider injecting this dependency
        log.debug("Using AI model: {}", model);
        return model;
    }

}
