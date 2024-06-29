package com.springboot.FlomadAIplanner.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.SessionScope;

@Configuration
public class AIConfig {

    @Bean
    @SessionScope
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

}
