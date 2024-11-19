package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageResponse;
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
    /**
     * Processes an AI message and returns the AI response with suggestions.
     *
     * @param aiMessage The user AI message DTO
     * @return The AIMessageResponse containing the main text and suggestions
     */
    public AIMessageResponse processEntityMessage(AIMessageDTO aiMessage) {
        log.info("Processing aiMessage with suggestions: {}; sessionId: {}", aiMessage.getMessage(), aiMessage.getSessionId());

        try {
            // Use the AgentFactory to select the appropriate agent
            Agent agent = agentFactory.getAgent(aiMessage);

            // Get the main response text
            String mainResponse = (String) agent.processCallMessage(aiMessage);

            // Generate suggestions (for now, static; replace with dynamic logic if needed)
            List<String> suggestions = List.of("Book a flight", "Check hotels", "Explore attractions", "View weather");

            log.info("Generated response: {} with suggestions: {}", mainResponse, suggestions);

            // Return the response with suggestions
            return new AIMessageResponse(mainResponse, suggestions);

        } catch (NullPointerException e) {
            log.error("NullPointerException occurred while processing the message: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing entity message", e);
        } catch (Exception e) {
            log.error("Exception occurred while processing the message: {}", e.getMessage(), e);
            throw e;
        }
    }

}
