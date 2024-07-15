package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.AgentFactory;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Constructor for ChatCallService.
     *
     * @param agentFactory           Factory for creating chat clients
     * @param aiFunctionDescriptions List of available AI function descriptions
     */
    public ChatCallService(AgentFactory agentFactory, List<String> aiFunctionDescriptions,
                           Map<String, String> promptsList) {
        super(agentFactory, aiFunctionDescriptions, promptsList);
        log.info("ChatCallService initialized with AI function descriptions: {}", aiFunctionDescriptions);
    }

    /**
     * Processes a aiMessage and determines the appropriate response.
     *
     * @param aiMessage The user aiMessage DTO
     * @return The response content
     */
    public String processMessage(AIMessageDTO aiMessage) {
        log.info("Processing aiMessage: {}; sessionId: {}", aiMessage.getMessage(), aiMessage.getSessionId());

        return (String) getChatAgent().processCallMessage(aiMessage);
    }

}
