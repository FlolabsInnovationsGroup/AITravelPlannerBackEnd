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
    public ChatCallService(AgentFactory agentFactory,
                           List<String> aiFunctionDescriptions,
                           Map<String, String> promptsList) {
        super(agentFactory, aiFunctionDescriptions, promptsList);
        log.info("ChatCallService initialized with the necessary AI function descriptions.");
        this.agentFactory = agentFactory;
    }

    /**
     * Processes an AI message and determines the appropriate response.
     *
     * @param aiMessage The user AI message DTO
     * @return The response content as a String
     */
    public String processMessage(AIMessageDTO aiMessage) {
        log.info("Processing aiMessage: {}; sessionId: {}", aiMessage.getMessage(), aiMessage.getSessionId());

        try {
            Agent agent = agentFactory.getAgent(aiMessage);
            return (String) agent.processCallMessage(aiMessage);
        } catch (Exception e) {
            log.error("Exception occurred while processing the message: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing chat message", e);
        }
    }

    /**
     * Processes an AI message and returns the AI response with suggestions.
     *
     * @param aiMessage The user AI message DTO
     * @return The AIMessageResponse containing the main text and suggestions
     */
    public <T> T processEntityMessage(AIMessageDTO aiMessage, Class<T> returnClassType) {
        log.info("Processing aiMessage with suggestions: {}; sessionId: {}", aiMessage.getMessage(), aiMessage.getSessionId());

        try {
            Agent agent = agentFactory.getAgent(aiMessage);
            return agent.processEntityMessage(aiMessage, returnClassType);
        } catch (UnsupportedOperationException e) {
            log.warn("Entity message processing is not supported by the selected agent: {}", e.getMessage());
            throw new RuntimeException("This agent does not support entity message processing.", e);
        } catch (Exception e) {
            log.error("Error occurred while processing entity message: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing entity-based chat message", e);
        }
    }
}