package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.AgentFactory;
import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.MessageType;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
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

    public EnhancedChatCallService(AgentFactory agentFactory, 
                                  List<String> aiFunctionDescriptions,
                                  @Qualifier("templatePrompts") Map<String, String> promptsList,
                                  MessageService messageService) {
        super(agentFactory, aiFunctionDescriptions, promptsList);
        this.messageService = messageService;
    }

    /**
     * Processes a chat message with explicit message persistence.
     * This method ensures both user input and AI response are saved to MongoDB.
     *
     * @param aiMessage The user AI message DTO
     * @return The AI response as a string
     */
    public String processCallMessage(AIMessageDTO aiMessage) {
        log.info("🔵 Processing call message: {}; sessionId: {}", aiMessage.getMessage(), aiMessage.getSessionId());

        try {
            // 1. Save user message BEFORE processing
            Message userMessage = saveUserMessage(aiMessage);
            log.info("💾 Saved user message: {}", userMessage.getId());

            // 2. Process with agent
            Agent agent = getChatAgent(aiMessage);
            String aiResponse = (String) agent.processCallMessage(aiMessage);

            // 3. Save AI response AFTER processing
            Message assistantMessage = saveAssistantMessage(aiMessage.getSessionId(), aiResponse);
            log.info("💾 Saved assistant message: {}", assistantMessage.getId());

            log.info("✅ Successfully processed and saved messages for session: {}", aiMessage.getSessionId());
            return aiResponse;

        } catch (Exception e) {
            log.error("❌ Exception occurred while processing the message: {}", e.getMessage(), e);
            
            // Save error message so user knows what happened
            try {
                saveAssistantMessage(aiMessage.getSessionId(), "I apologize, but I encountered an error processing your request. Please try again.");
            } catch (Exception saveError) {
                log.error("❌ Failed to save error message: {}", saveError.getMessage());
            }
            
            throw new RuntimeException("Error processing chat message", e);
        }
    }

    /**
     * Processes an AI message and returns the AI response with explicit message persistence.
     *
     * @param aiMessage The user AI message DTO  
     * @param returnClassType The expected return type
     * @return The AI response of the specified type
     */
    public <T> T processEntityMessage(AIMessageDTO aiMessage, Class<T> returnClassType) {
        log.info("🔵 Processing entity message with suggestions: {}; sessionId: {}", aiMessage.getMessage(), aiMessage.getSessionId());

        try {
            // 1. Save user message BEFORE processing
            Message userMessage = saveUserMessage(aiMessage);
            log.info("💾 Saved user message: {}", userMessage.getId());

            // 2. Process with agent
            Agent agent = getChatAgent(aiMessage);
            T aiResponse = agent.processEntityMessage(aiMessage, returnClassType);

            // 3. Save AI response AFTER processing
            String responseText = extractTextFromResponse(aiResponse);
            Message assistantMessage = saveAssistantMessage(aiMessage.getSessionId(), responseText);
            log.info("💾 Saved assistant message: {}", assistantMessage.getId());

            log.info("✅ Successfully processed and saved entity messages for session: {}", aiMessage.getSessionId());
            return aiResponse;

        } catch (UnsupportedOperationException e) {
            log.warn("Entity message processing is not supported by the selected agent: {}", e.getMessage());
            
            // Save error message
            try {
                saveAssistantMessage(aiMessage.getSessionId(), "This type of request is not currently supported. Please try a different approach.");
            } catch (Exception saveError) {
                log.error("❌ Failed to save error message: {}", saveError.getMessage());
            }
            
            throw new RuntimeException("This agent does not support entity message processing.", e);
            
        } catch (Exception e) {
            log.error("❌ Error occurred while processing entity message: {}", e.getMessage(), e);
            
            // Save error message
            try {
                saveAssistantMessage(aiMessage.getSessionId(), "I apologize, but I encountered an error processing your request. Please try again.");
            } catch (Exception saveError) {
                log.error("❌ Failed to save error message: {}", saveError.getMessage());
            }
            
            throw new RuntimeException("Error processing entity-based chat message", e);
        }
    }

    /**
     * Save user message to database
     */
    private Message saveUserMessage(AIMessageDTO aiMessage) {
        Message userMessage = Message.builder()
                .sessionId(aiMessage.getSessionId())
                .content(aiMessage.getMessage())
                .messageType(MessageType.USER)
                .createdAt(LocalDateTime.now())
                .metadata(createUserMetadata(aiMessage))
                .build();

        return messageService.save(userMessage);
    }

    /**
     * Save assistant message to database
     */
    private Message saveAssistantMessage(String sessionId, String content) {
        Message assistantMessage = Message.builder()
                .sessionId(sessionId)
                .content(content)
                .messageType(MessageType.ASSISTANT)
                .createdAt(LocalDateTime.now())
                .metadata(createAssistantMetadata())
                .build();

        return messageService.save(assistantMessage);
    }

    /**
     * Create metadata for user messages
     */
    private Map<String, Object> createUserMetadata(AIMessageDTO aiMessage) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "user_input");
        metadata.put("timestamp", LocalDateTime.now().toString());
        
        if (aiMessage.getMedia() != null) {
            metadata.put("hasAudio", true);
        }
        
        return metadata;
    }

    /**
     * Create metadata for assistant messages
     */
    private Map<String, Object> createAssistantMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "ai_assistant");
        metadata.put("timestamp", LocalDateTime.now().toString());
        metadata.put("model", "gpt-3.5-turbo"); // Could be made configurable
        
        return metadata;
    }

    /**
     * Extract text content from various response types
     */
    private String extractTextFromResponse(Object response) {
        if (response == null) {
            return "No response generated.";
        }
        
        if (response instanceof String) {
            return (String) response;
        }
        
        // For structured responses, try to extract meaningful text
        try {
            // Check if response has a common text field
            if (response.getClass().getMethod("getText") != null) {
                Object text = response.getClass().getMethod("getText").invoke(response);
                if (text instanceof String) {
                    return (String) text;
                }
            }
        } catch (Exception e) {
            // Ignore reflection errors
        }
        
        try {
            // Check if response has a message field
            if (response.getClass().getMethod("getMessage") != null) {
                Object message = response.getClass().getMethod("getMessage").invoke(response);
                if (message instanceof String) {
                    return (String) message;
                }
            }
        } catch (Exception e) {
            // Ignore reflection errors
        }
        
        // Fallback to toString()
        String responseText = response.toString();
        
        // If toString is too long, truncate it
        if (responseText.length() > 1000) {
            responseText = responseText.substring(0, 1000) + "... [response truncated]";
        }
        
        return responseText;
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