package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
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

    private final AgentFactory agentFactory;
    private final List<String> aiFunctionDescriptions;
    private final Map<String, String> promptsList;

    @Value("classpath:/templates/PromptAndFunctionSuggestionsPrompt.txt")
    @Setter
    private Resource promptAndFunctionSuggestionsPrompt;

    public AbstractChatService(AgentFactory agentFactory, List<String> aiFunctionDescriptions,
                               @Qualifier("templatePrompts") Map<String, String> promptsList) {
        this.agentFactory = agentFactory;
        this.aiFunctionDescriptions = Collections.unmodifiableList(aiFunctionDescriptions);
        this.promptsList = promptsList;
        log.info("Chat service initialized with AI function descriptions: {}", aiFunctionDescriptions);
    }

    /**
     * Retrieves the appropriate Agent based on the AI message.
     *
     * @param aiMessage The AI message to process
     * @return An instance of Agent
     */
    protected Agent getChatAgent(AIMessageDTO aiMessage) {
        log.debug("Getting agent for the AI message.");
        return agentFactory.getAgent(aiMessage);
    }
}

