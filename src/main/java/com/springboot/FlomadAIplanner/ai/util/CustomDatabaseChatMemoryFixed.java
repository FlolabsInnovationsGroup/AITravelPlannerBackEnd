package com.springboot.FlomadAIplanner.ai.util;

import com.springboot.FlomadAIplanner.converter.ChatMemoryMessageConverter;
import com.springboot.FlomadAIplanner.converter.ChatMemoryMessageRevertConverter;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Database-backed ChatMemory implementation that persists messages to MongoDB
 * and provides conversation context for Spring AI ChatClient.
 */
@Slf4j
@Component("customDatabaseChatMemoryFixed")
public class CustomDatabaseChatMemoryFixed implements ChatMemory {
    
    private final static String DEFAULT_CONVERSATION_ID = "default";
    
    @Value("${chat.memory.max-messages:100}")
    private int maxMessages;
    
    @Value("${chat.memory.max-tokens:800}")
    private int maxTokensPerMessage;
    
    @Value("${chat.memory.estimated-tokens-per-char:4}")
    private int estimatedTokensPerChar;
    
    private final MessageService messageService;
    private final ChatMemoryMessageConverter messageConverter;
    private final ChatMemoryMessageRevertConverter messageRevertConverter;

    public CustomDatabaseChatMemoryFixed(MessageService messageService,
                                         ChatMemoryMessageConverter messageConverter,
                                         ChatMemoryMessageRevertConverter messageRevertConverter) {
        this.messageService = messageService;
        this.messageConverter = messageConverter;
        this.messageRevertConverter = messageRevertConverter;
        
        // Initialize default values if not set by @Value injection
        if (this.maxMessages <= 0) {
            this.maxMessages = 100;
        }
        if (this.maxTokensPerMessage <= 0) {
            this.maxTokensPerMessage = 800;
        }
        if (this.estimatedTokensPerChar <= 0) {
            this.estimatedTokensPerChar = 4;
        }
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages.isEmpty() || DEFAULT_CONVERSATION_ID.equals(conversationId)) {
            log.debug("Skipping add for empty messages or default conversation: {}", conversationId);
            return;
        }

        log.info("🔵 Adding {} messages to conversation: {}", messages.size(), conversationId);

        try {
            // Convert Spring AI messages to our Message entities
            List<com.springboot.FlomadAIplanner.model.entities.Message> messageEntities = messages.stream()
                    .map(msg -> {
                        com.springboot.FlomadAIplanner.model.entities.Message entity = messageConverter.convert(msg);
                        if (entity != null) {
                            entity.setSessionId(conversationId);
                            
                            // Truncate content if too long
                            String content = entity.getContent();
                            if (content != null && estimatedTokensPerChar > 0) {
                                int estimatedTokens = content.length() / estimatedTokensPerChar;
                                if (estimatedTokens > maxTokensPerMessage) {
                                    int maxChars = maxTokensPerMessage * estimatedTokensPerChar;
                                    String truncated = content.substring(0, Math.min(content.length(), maxChars)) + "... [truncated]";
                                    entity.setContent(truncated);
                                    log.info("Truncated message from {} tokens to {} tokens", estimatedTokens, maxTokensPerMessage);
                                }
                            }
                        }
                        return entity;
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            // Save each message to database
            for (com.springboot.FlomadAIplanner.model.entities.Message messageEntity : messageEntities) {
                messageService.save(messageEntity);
                log.debug("💾 Saved message to database: {}", messageEntity.getId());
            }

            // Cleanup old messages if needed
            cleanupOldMessages(conversationId);

            log.info("✅ Successfully added {} messages to conversation: {}", messageEntities.size(), conversationId);

        } catch (Exception e) {
            log.error("❌ Error adding messages to conversation {}: {}", conversationId, e.getMessage(), e);
            throw new RuntimeException("Failed to add messages to chat memory", e);
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        if (DEFAULT_CONVERSATION_ID.equals(conversationId) || lastN <= 0) {
            log.debug("Returning empty list for conversation: {} or lastN: {}", conversationId, lastN);
            return Collections.emptyList();
        }

        log.info("🔍 Retrieving last {} messages from conversation: {}", lastN, conversationId);

        try {
            // Get messages from database, ordered by creation time
            List<com.springboot.FlomadAIplanner.model.entities.Message> messageEntities = 
                messageService.getMessagesBySessionId(conversationId);

            if (messageEntities.isEmpty()) {
                log.info("No messages found for conversation: {}", conversationId);
                return Collections.emptyList();
            }

            // Get the last N messages
            int start = Math.max(0, messageEntities.size() - lastN);
            List<com.springboot.FlomadAIplanner.model.entities.Message> recentMessages = 
                messageEntities.subList(start, messageEntities.size());

            // Convert back to Spring AI Message format
            List<Message> result = recentMessages.stream()
                    .map(messageRevertConverter::convert)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            log.info("📊 Found {} messages for conversation: {}", result.size(), conversationId);
            return result;

        } catch (Exception e) {
            log.error("❌ Error retrieving messages for conversation {}: {}", conversationId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public void clear(String conversationId) {
        if (DEFAULT_CONVERSATION_ID.equals(conversationId)) {
            log.debug("Skipping clear for default conversation: {}", conversationId);
            return;
        }

        log.info("🗑️ Clearing conversation: {}", conversationId);

        try {
            List<com.springboot.FlomadAIplanner.model.entities.Message> messages = 
                messageService.getMessagesBySessionId(conversationId);
            
            for (com.springboot.FlomadAIplanner.model.entities.Message message : messages) {
                messageService.delete(message.getId());
            }

            log.info("✅ Cleared {} messages for conversation: {}", messages.size(), conversationId);

        } catch (Exception e) {
            log.error("❌ Error clearing conversation {}: {}", conversationId, e.getMessage(), e);
            throw new RuntimeException("Failed to clear chat memory", e);
        }
    }

    /**
     * Cleanup old messages if the conversation exceeds the maximum message limit
     */
    private void cleanupOldMessages(String conversationId) {
        try {
            List<com.springboot.FlomadAIplanner.model.entities.Message> allMessages = 
                messageService.getMessagesBySessionId(conversationId);

            if (allMessages.size() > maxMessages) {
                int excessCount = allMessages.size() - maxMessages;
                
                // Delete the oldest messages (keep the most recent ones)
                List<com.springboot.FlomadAIplanner.model.entities.Message> oldMessages = 
                    allMessages.subList(0, excessCount);

                for (com.springboot.FlomadAIplanner.model.entities.Message oldMessage : oldMessages) {
                    messageService.delete(oldMessage.getId());
                }

                log.info("🧹 Cleaned up {} old messages for conversation: {}. Remaining: {}", 
                        excessCount, conversationId, maxMessages);
            }

        } catch (Exception e) {
            log.error("❌ Error during cleanup for conversation {}: {}", conversationId, e.getMessage(), e);
        }
    }

    /**
     * Get the total number of messages for a conversation (for debugging)
     */
    public int getMessageCount(String conversationId) {
        try {
            return messageService.getMessagesBySessionId(conversationId).size();
        } catch (Exception e) {
            log.error("❌ Error getting message count for conversation {}: {}", conversationId, e.getMessage());
            return 0;
        }
    }
}