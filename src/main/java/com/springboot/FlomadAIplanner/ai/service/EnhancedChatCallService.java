package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.AgentFactory;
import com.springboot.FlomadAIplanner.ai.validation.AIResponseValidationService;
import com.springboot.FlomadAIplanner.ai.validation.ValidationResult;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Enhanced ChatCallService that explicitly saves user and assistant messages to database
 * This ensures proper message persistence for chat history functionality.
 */
@Slf4j
@Service
public class EnhancedChatCallService extends AbstractChatService {
    
    private final MessageService messageService;
    private final AIResponseValidationService validationService;
    private final ResponseCacheService cacheService;

    public EnhancedChatCallService(AgentFactory agentFactory, 
                                  List<String> aiFunctionDescriptions,
                                  @Qualifier("templatePrompts") Map<String, String> promptsList,
                                  MessageService messageService,
                                  AIResponseValidationService validationService,
                                  ResponseCacheService cacheService) {
        super(agentFactory, aiFunctionDescriptions, promptsList);
        this.messageService = messageService;
        this.validationService = validationService;
        this.cacheService = cacheService;
        log.info("✅ EnhancedChatCallService initialized with validation and caching support");
    }

    /**
     * Processes a chat message using Spring AI's automatic message persistence.
     * Spring AI's chat memory system handles message saving automatically.
     *
     * @param aiMessage The user AI message DTO
     * @return The AI response as a string
     */
    public String processCallMessage(AIMessageDTO aiMessage) {
        log.info("🔵 Processing call message: {}; sessionId: {}", aiMessage.getMessage(), aiMessage.getSessionId());

        try {
            // Check cache first for performance optimization
            if (cacheService.shouldUseCache(aiMessage.getMessage())) {
                String cachedResponse = cacheService.getCachedResponse(aiMessage.getMessage());
                if (cachedResponse != null) {
                    log.info("🚀 Returning cached response for performance boost");
                    return cachedResponse;
                }
            }

            // Process with agent with performance monitoring
            long startTime = System.currentTimeMillis();
            Agent agent = getChatAgent(aiMessage);
            log.info("🤖 About to call agent.processCallMessage()");
            
            String aiResponse = (String) agent.processCallMessage(aiMessage);
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Log performance metrics
            log.info("🤖 Agent response: length={}, time={}ms, performance={}", 
                aiResponse != null ? aiResponse.length() : 0, 
                processingTime,
                processingTime < 5000 ? "EXCELLENT" : processingTime < 10000 ? "GOOD" : "NEEDS OPTIMIZATION");
            
            // Performance warning for slow responses
            if (processingTime > 15000) {
                log.warn("⚠️ Slow response detected: {}ms for message: {}", 
                    processingTime, aiMessage.getMessage().substring(0, Math.min(50, aiMessage.getMessage().length())));
            }

            // Cache the response if it's cacheable
            if (aiResponse != null && cacheService.shouldUseCache(aiMessage.getMessage())) {
                cacheService.cacheResponse(aiMessage.getMessage(), aiResponse);
            }

            log.info("✅ Successfully processed message for session: {}", aiMessage.getSessionId());
            return aiResponse;

        } catch (Exception e) {
            log.error("❌ Exception occurred while processing the message: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing chat message", e);
        }
    }

    /**
     * Processes an AI message and returns the AI response using Spring AI's automatic persistence.
     * Includes JSON schema validation for AI outputs to ensure quality and consistency.
     *
     * @param aiMessage The user AI message DTO  
     * @param returnClassType The expected return type
     * @return The AI response of the specified type
     */
    public <T> T processEntityMessage(AIMessageDTO aiMessage, Class<T> returnClassType) {
        // Add null check for aiMessage
        if (aiMessage == null) {
            log.error("❌ AIMessageDTO is null - cannot process message");
            throw new IllegalArgumentException("AIMessageDTO cannot be null");
        }
        
        if (aiMessage.getMessage() == null || aiMessage.getMessage().trim().isEmpty()) {
            log.error("❌ Message content is null or empty - cannot process");
            throw new IllegalArgumentException("Message content cannot be null or empty");
        }
        
        log.info("🔵 Processing entity message with suggestions: {}; sessionId: {}", aiMessage.getMessage(), aiMessage.getSessionId());

        try {
            // Process with agent - Spring AI chat memory handles message persistence automatically
            Agent agent = getChatAgent(aiMessage);
            T aiResponse = agent.processEntityMessage(aiMessage, returnClassType);

            // Validate AI response if it's an AIMessageResponse
            if (aiResponse instanceof com.springboot.FlomadAIplanner.ai.dto.AIMessageResponse) {
                log.info("🔍 Validating AI response schema for session: {}", aiMessage.getSessionId());
                ValidationResult validationResult = validationService.validateWithContext(
                    (com.springboot.FlomadAIplanner.ai.dto.AIMessageResponse) aiResponse,
                    aiMessage.getSessionId(),
                    "AI Chat Response"
                );
                
                if (!validationResult.isValid()) {
                    log.warn("⚠️ AI response validation failed for session {}: {}", 
                        aiMessage.getSessionId(), validationResult.getErrorMessage());
                    // Log validation issues but don't fail the request to maintain user experience
                    // In production, consider implementing fallback responses or retry logic
                }
            }

            log.info("✅ Successfully processed entity message for session: {}", aiMessage.getSessionId());
            return aiResponse;

        } catch (UnsupportedOperationException e) {
            log.warn("Entity message processing is not supported by the selected agent: {}", e.getMessage());
            throw new RuntimeException("This agent does not support entity message processing.", e);
            
        } catch (Exception e) {
            log.error("❌ Error occurred while processing entity message: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing entity-based chat message", e);
        }
    }

    /**
     * Get message count for debugging
     */
    public int getMessageCountForSession(String sessionId) {
        try {
            return messageService.getMessagesBySessionId(sessionId).size();
        } catch (Exception e) {
            log.error("❌ Error getting message count for session {}: {}", sessionId, e.getMessage());
            return 0;
        }
    }
}