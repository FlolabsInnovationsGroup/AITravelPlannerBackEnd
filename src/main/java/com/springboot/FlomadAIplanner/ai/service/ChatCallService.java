package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.AgentFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ChatCallService extends AbstractChatService to provide specific functionality for processing chat messages.
 * This service analyzes the message content to determine required functions and processes the message accordingly.
 */
@Slf4j
@Service
public class ChatCallService extends AbstractChatService {

    private final AgentFactory agentFactory;

    @Autowired
    public ChatCallService(AgentFactory agentFactory, List<String> aiFunctionDescriptions,
                           Map<String, String> promptsList) {
        super(agentFactory, aiFunctionDescriptions, promptsList);
        log.info("ChatCallService initialized with necessary AI function descriptions.");
        this.agentFactory = agentFactory;
    }

    /**
     * Processes an AI message and determines the appropriate response.
     *
     * @param aiMessage The user AI message DTO
     * @return The response content
     */
    public String processMessage(AIMessageDTO aiMessage) {
        log.info("Processing aiMessage: {}; sessionId: {}", aiMessage.getMessage(), aiMessage.getSessionId());

        try {
            // Use the AgentFactory to select the appropriate agent based on the input message
            Agent agent = agentFactory.getAgent(aiMessage);
            return (String) agent.processCallMessage(aiMessage);
        } catch (NullPointerException e) {
            // Log the error and handle the exception
            log.error("NullPointerException occurred while processing the message: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing chat message", e);
        } catch (Exception e) {
            log.error("Exception occurred while processing the message: {}", e.getMessage(), e);
            throw e;
        }
    }

}
