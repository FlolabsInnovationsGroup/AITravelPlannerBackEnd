package com.springboot.FlomadAIplanner.config;

import com.springboot.FlomadAIplanner.ai.util.CustomDatabaseChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    @Bean
    public ChatMemory conversationContext() {
        return new CustomDatabaseChatMemory();
    }
}
