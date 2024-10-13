package com.springboot.FlomadAIplanner.ai.factory;

import com.springboot.FlomadAIplanner.ai.config.cnst.AIModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@SuppressWarnings("unused")
public class ChatClientFactory {

    private final Map<AIModel, ChatClient> chatClients;

    @Autowired
    public ChatClientFactory(@Qualifier("chatClients") Map<AIModel, ChatClient> chatClients) {
        log.info("ChatClientFactory initialized with chat clients: {}", chatClients);
        this.chatClients = chatClients;
    }

    public ChatClient getClient(AIModel model) {
        ChatClient chatClient = chatClients.get(model);
        if (chatClient == null) {
            throw new IllegalArgumentException("Unknown model: " + model);
        }
        return chatClient;
    }
}
