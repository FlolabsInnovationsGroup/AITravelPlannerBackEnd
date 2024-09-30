package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.AgentFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * ChatStreamService processes messages in a streaming manner.
 * It extends AbstractChatService to leverage common chat-related functionalities.
 */
@Slf4j
@Service
public class ChatStreamService extends AbstractChatService {

    /**
     * Constructor for ChatCallService.
     *
     * @param agentFactory           Factory for creating chat clients
     * @param aiFunctionDescriptions List of available AI function descriptions
     */
    public ChatStreamService(AgentFactory agentFactory, List<String> aiFunctionDescriptions,
                             Map<String, String> promptsList) {
        super(agentFactory, aiFunctionDescriptions, promptsList);
        log.info("ChatCallService initialized with AI function descriptions: {}", aiFunctionDescriptions);
    }

    /**
     * Processes a aiMessage and determines the appropriate response.
     *
     * @param aiMessage The user aiMessage DTO
     * @param sessionId  The user's guest session ID
     * @return The response content
     */
    @SuppressWarnings("unchecked")
    public Flux<String> processMessage(AIMessageDTO aiMessage, String sessionId) {
        log.info("Processing aiMessage: {}; sessionId: {}", aiMessage.getMessage(), sessionId);

        return (Flux<String>) getChatAgent().processStreamMessage(aiMessage, sessionId);
    }
}