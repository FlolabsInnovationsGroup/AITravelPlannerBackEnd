package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.AgentFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
     * Constructor for ChatStreamService.
     *
     * @param agentFactory           Factory for creating chat clients
     * @param aiFunctionDescriptions List of available AI function descriptions
     * @param promptsList            Map of prompt file names and their content
     */
    public ChatStreamService(AgentFactory agentFactory, List<String> aiFunctionDescriptions,
                             @Qualifier("templatePrompts") Map<String, String> promptsList) {
        super(agentFactory, aiFunctionDescriptions, promptsList);
        log.info("ChatStreamService initialized with necessary AI function descriptions.");
    }

    /**
     * Processes an AI message and streams the appropriate response.
     *
     * @param aiMessage The user AI message DTO
     * @return A stream of response content as Flux<String>
     */
    @SuppressWarnings("unchecked")
    public Flux<String> processMessage(AIMessageDTO aiMessage) {
        log.info("Processing AI message: {}; sessionId: {}", aiMessage.getMessage(), aiMessage.getSessionId());

        // Get the agent that supports this type of message
        Agent agent = getChatAgent(aiMessage);

        // Process the message using the selected agent
        return (Flux<String>) agent.processStreamMessage(aiMessage);
    }
}
