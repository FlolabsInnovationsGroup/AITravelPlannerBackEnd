package com.springboot.FlomadAIplanner.ai.factory;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Factory class responsible for selecting the appropriate Agent to handle an AI message.
 * The factory retrieves agents based on the message type, or the default agent when needed.
 */
@Slf4j
@Service
public class AgentFactory {

    private final List<Agent> agents;
    private final Agent defaultAgent; // Added field for the default agent

    /**
     * Constructor for AgentFactory.
     *
     * @param agents       A list of available agents that implement the Agent interface.
     * @param defaultAgent The default agent to be used when no specific agent is selected.
     */
    @Autowired
    public AgentFactory(List<Agent> agents, @Qualifier("openAiAgent") Agent defaultAgent) {
        log.info("AgentFactory initialized with {} agents.", agents.size());
        this.agents = agents;
        this.defaultAgent = defaultAgent; // Initialize default agent
    }

    /**
     * Retrieves the appropriate agent for processing the given AIMessageDTO.
     *
     * @param aiMessage The AI message to process.
     * @return The Agent capable of handling the message.
     * @throws IllegalArgumentException if no suitable agent is found for the message type.
     */
    public Agent getAgent(AIMessageDTO aiMessage) {
        log.info("Selecting an agent for message: {}", aiMessage.getMessage());

        // Loop through available agents to find a match
        return agents.stream()
                .filter(agent -> {
                    boolean supports = agent.supports(aiMessage);
                    log.debug("Checking agent {}: supports={}", agent.getClass().getSimpleName(), supports);
                    return supports;
                })
                .findFirst()
                .orElseThrow(() -> {
                    log.error("No suitable agent found for message: {}", aiMessage.getMessage());
                    return new IllegalArgumentException("No suitable agent found for the given message type.");
                });
    }

    /**
     * Retrieves the default agent if no specific match is required.
     * This can be used for general fallback processing or non-specific tasks.
     *
     * @return The default agent.
     * @throws IllegalStateException if no agents are available.
     */
    public Agent getDefaultAgent() {
        log.info("Retrieving default agent.");
        return defaultAgent; // Return the explicitly defined default agent
    }
}

