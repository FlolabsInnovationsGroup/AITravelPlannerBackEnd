package com.springboot.FlomadAIplanner.config;

import com.springboot.FlomadAIplanner.ai.util.CustomDatabaseChatMemoryFixed;
import com.springboot.FlomadAIplanner.converter.ChatMemoryMessageConverter;
import com.springboot.FlomadAIplanner.converter.ChatMemoryMessageRevertConverter;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for database-backed chat memory
 * This replaces the in-memory chat memory with proper database persistence
 */
@Configuration
public class ChatMemoryDatabaseConfig {

    /**
     * Creates the primary database-backed chat memory bean
     * This will replace the existing in-memory implementation
     */
    @Bean
    @Primary
    public ChatMemory databaseChatMemory(MessageService messageService,
                                        ChatMemoryMessageConverter messageConverter,
                                        ChatMemoryMessageRevertConverter messageRevertConverter) {
        return new CustomDatabaseChatMemoryFixed(messageService, messageConverter, messageRevertConverter);
    }

    /**
     * Alternative bean name for explicit dependency injection
     */
    @Bean("customDatabaseChatMemory")
    public ChatMemory customDatabaseChatMemory(MessageService messageService,
                                              ChatMemoryMessageConverter messageConverter,
                                              ChatMemoryMessageRevertConverter messageRevertConverter) {
        return new CustomDatabaseChatMemoryFixed(messageService, messageConverter, messageRevertConverter);
    }
}