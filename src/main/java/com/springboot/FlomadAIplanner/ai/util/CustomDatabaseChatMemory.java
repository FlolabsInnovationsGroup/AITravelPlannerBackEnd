package com.springboot.FlomadAIplanner.ai.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
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
    
    @Value("${chat.memory.max-messages:5}")
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

            // Add only the most recent messages
            int start = Math.max(0, messages.size() - maxMessages);
            List<Message> recentMessages = messages.subList(start, messages.size());
            this.messages.addAll(recentMessages);

            // Keep only the last maxMessages
            if (this.messages.size() > maxMessages) {
                this.messages.subList(0, this.messages.size() - maxMessages).clear();
            }

            // Clean up old messages periodically
            cleanupOldMessages();
        } else {
            log.info("Not adding message to 'default' conversation");
        }
    }

    private void cleanupOldMessages() {
        if (messages.size() > maxMessages * 2) {
            synchronized (messages) {
                if (messages.size() > maxMessages * 2) {
                    int excess = messages.size() - maxMessages;
                    messages.subList(0, excess).clear();
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