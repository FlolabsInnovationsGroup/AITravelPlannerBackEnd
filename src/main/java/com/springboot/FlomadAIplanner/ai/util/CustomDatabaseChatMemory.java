package com.springboot.FlomadAIplanner.ai.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component("customDatabaseChatMemory")
public class CustomDatabaseChatMemory implements ChatMemory {
    private final static String DEFAULT_CONVERSATION_ID = "default";
    private final static int MAX_TOKENS_PER_MESSAGE = 800; // Conservative limit to prevent overflow
    private final static int ESTIMATED_TOKENS_PER_CHAR = 4; // Conservative estimate
    
    @Value("${chat.memory.max-messages:2}")
    private int maxMessages;
    
    private final List<Message> messages = new CopyOnWriteArrayList<>();

    public CustomDatabaseChatMemory() {
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages.isEmpty()) {
            return;
        }

        if (!DEFAULT_CONVERSATION_ID.equals(conversationId)) {
            log.info("Adding {} messages to conversation: {}", messages.size(), conversationId);

            synchronized (this.messages) {
                // Clean up first if we're about to exceed the limit
                if (this.messages.size() + messages.size() > maxMessages) {
                    cleanupOldMessages();
                }

                // Add only the most recent messages
                int start = Math.max(0, messages.size() - maxMessages);
                List<Message> recentMessages = new ArrayList<>(messages.subList(start, messages.size()));
                
                // Truncate messages to prevent token limit issues
                for (int i = 0; i < recentMessages.size(); i++) {
                    Message msg = recentMessages.get(i);
                    String content = msg.toString();
                    int estimatedTokens = content.length() / ESTIMATED_TOKENS_PER_CHAR;
                    
                    if (estimatedTokens > MAX_TOKENS_PER_MESSAGE) {
                        int maxChars = MAX_TOKENS_PER_MESSAGE * ESTIMATED_TOKENS_PER_CHAR;
                        String truncated = content.substring(0, Math.min(content.length(), maxChars)) + "... [truncated]";
                        recentMessages.set(i, new UserMessage(truncated));
                        log.info("Truncated message from {} tokens to {} tokens", estimatedTokens, MAX_TOKENS_PER_MESSAGE);
                    }
                }

                this.messages.addAll(recentMessages);
                log.info("Added {} messages. Current size: {}", recentMessages.size(), this.messages.size());
            }
        } else {
            log.info("Not adding message to 'default' conversation");
        }
    }

    private void cleanupOldMessages() {
        if (messages.size() > maxMessages) {
            synchronized (messages) {
                if (messages.size() > maxMessages) {
                    // Keep only the most recent messages
                    int excess = messages.size() - maxMessages;
                    messages.subList(0, excess).clear();
                    log.info("Cleaned up {} old messages. Current size: {}", excess, messages.size());
                }
            }
        }
    }    @Override
    public List<Message> get(String conversationId, int lastN) {
        log.info("Retrieving last {} messages from conversation: {}", lastN, conversationId);

        if (lastN <= 0) {
            return Collections.emptyList();
        }

        int start = Math.max(0, messages.size() - lastN);
        List<Message> result = messages.subList(start, messages.size());
        log.info("Found {} messages", result.size());
        return result;
    }

    @Override
    public void clear(String conversationId) {
        messages.clear();
    }

}