package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.AgentFactory;
import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.service.model.MessageService;

import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    @Autowired
    private final MessageService messageService;

    public ChatCallService(AgentFactory agentFactory, List<String> aiFunctionDescriptions,
                           Map<String, String> promptsList,MessageService messageService) {
        super(agentFactory, aiFunctionDescriptions, promptsList);
        this.messageService = messageService;
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
        String aiResponse = (String) getChatAgent().processCallMessage(aiMessage);
        return aiResponse;
    }

}
