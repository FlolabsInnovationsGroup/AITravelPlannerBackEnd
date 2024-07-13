package com.springboot.FlomadAIplanner.ai.util;

import com.springboot.FlomadAIplanner.converter.Converter;
import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component("customDatabaseChatMemory")
public class CustomDatabaseChatMemory implements ChatMemory {

    private final Converter<org.springframework.ai.chat.messages.Message, Message> messageConverter;
    private final Converter<Message, org.springframework.ai.chat.messages.Message> messageRevertConverter;
    private final MessageService messageService;

    public CustomDatabaseChatMemory(Converter<org.springframework.ai.chat.messages.Message, Message> messageConverter, Converter<Message, org.springframework.ai.chat.messages.Message> messageRevertConverter, MessageService messageService) {
        this.messageConverter = messageConverter;
        this.messageRevertConverter = messageRevertConverter;
        this.messageService = messageService;
    }


    @Override
    public void add(String conversationId, List<org.springframework.ai.chat.messages.Message> messages) {
        List<Message> newMessages = new ArrayList<>();
        for (org.springframework.ai.chat.messages.Message message : messages) {
            log.info("Adding message: \"{}\",  to conversation: {}",
                    StringUtils.safeSubstring(message.getContent(), 50), conversationId);

            Message entity = this.messageConverter.convert(message);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setSessionId(conversationId);

            newMessages.add(entity);
        }

        this.messageService.saveAll(newMessages);
    }

    @Override
    public List<org.springframework.ai.chat.messages.Message> get(String conversationId, int lastN) {
        log.info("Retrieving last {} messages from conversation: {}", lastN, conversationId);
        List<Message> foundMessages = messageService.findAllBySessionId(conversationId);
        // convert
        List<org.springframework.ai.chat.messages.Message> all =
                foundMessages.stream().map(messageRevertConverter::convert).toList();

        return all.stream().skip(Math.max(0, all.size() - lastN)).toList();
    }

    @Override
    public void clear(String conversationId) {
        messageService.deleteBySessionId(conversationId);
    }

}