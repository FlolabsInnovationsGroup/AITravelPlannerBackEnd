package com.springboot.FlomadAIplanner.ai.factory;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Factory class responsible for selecting the appropriate Agent to handle an AI message.
 * The factory loops through all registered agents to find one that supports the given message type.
 */
@Slf4j
@Service
public class AgentFactory {

    private final List<Agent> agents;

    /**
     * Constructor for AgentFactory.
     *
     * @param agents A list of available agents that implement the Agent interface.
     */
    @Autowired
    public AgentFactory(List<Agent> agents) {
        log.info("AgentFactory initialized with {} agents.", agents.size());
        this.agents = agents;
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

        return agents.stream()
                .findFirst()
                .orElseThrow(() -> {
                    log.error("No agents available in the system.");
                    return new IllegalStateException("No agents are registered in the system.");
                });
    }
}

