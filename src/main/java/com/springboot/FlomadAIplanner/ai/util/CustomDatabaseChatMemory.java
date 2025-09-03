package com.springboot.FlomadAIplanner.ai.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component("customDatabaseChatMemory")
public class CustomDatabaseChatMemory implements ChatMemory {
    private final static String DEFAULT_CONVERSATION_ID = "default";
    private final static int DEFAULT_MAX_MESSAGES = 5;
    private final List<Message> messages = new ArrayList<>();

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
            int start = Math.max(0, messages.size() - DEFAULT_MAX_MESSAGES);
            List<Message> recentMessages = messages.subList(start, messages.size());
            this.messages.addAll(recentMessages);

            // Keep only the last maxMessages
            if (this.messages.size() > DEFAULT_MAX_MESSAGES) {
                this.messages.subList(0, this.messages.size() - DEFAULT_MAX_MESSAGES).clear();
            }
        } else {
            log.info("Not adding message to 'default' conversation");
        }
    }

    @Override
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