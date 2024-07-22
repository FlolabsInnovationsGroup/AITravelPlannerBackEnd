package com.springboot.FlomadAIplanner.ai.util;

import com.springboot.FlomadAIplanner.converter.Converter;
import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import com.springboot.FlomadAIplanner.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component("customDatabaseChatMemory")
public class CustomDatabaseChatMemory implements ChatMemory {

    @Autowired
    private HttpServletRequest request;

    private JwtUtil jwtUtil;

    private final Converter<org.springframework.ai.chat.messages.Message, Message> messageConverter;
    private final Converter<Message, org.springframework.ai.chat.messages.Message> messageRevertConverter;
    private final MessageService messageService;

    public CustomDatabaseChatMemory(
        Converter<org.springframework.ai.chat.messages.Message, Message> messageConverter, 
        Converter<Message, org.springframework.ai.chat.messages.Message> messageRevertConverter, 
        MessageService messageService,JwtUtil jwtUtil) {
        this.messageConverter = messageConverter;
        this.messageRevertConverter = messageRevertConverter;
        this.messageService = messageService;
        this.jwtUtil = jwtUtil;
    }


    @Override
    public void add(String conversationId, List<org.springframework.ai.chat.messages.Message> messages) {
        // check if existance of the session in DB

        final String authorizationHeader = this.request.getHeader("Authorization");
        String username = null;
        String jwt = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = this.jwtUtil.extractUsername(jwt);
        }
        log.info("add function received username : {}", username);


        List<Message> newMessages = new ArrayList<>();
        for (org.springframework.ai.chat.messages.Message message : messages) {
            log.info("Adding message: \"{}\",  to conversation: {}",
                    StringUtils.substring(message.getContent(), 0, 50), conversationId);

            Message entity = this.messageConverter.convert(message);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setSessionId(conversationId);
            entity.setUsername(username);

            newMessages.add(entity);
        }

        this.messageService.saveAll(newMessages);
    }

    @Override
    public List<org.springframework.ai.chat.messages.Message> get(String conversationId, int lastN) {
        log.info("Retrieving last {} messages from conversation: {}", lastN, conversationId);

        List<Message> foundMessages;
        if (lastN > 0) {
            foundMessages = messageService.getMessagesBySessionIdWithLimit(conversationId, lastN);
        } else {
            foundMessages = Collections.emptyList();
        }

        log.info("Found {} messages", foundMessages.size());
        return foundMessages.stream()
                .map(messageRevertConverter::convert)
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        messageService.deleteBySessionId(conversationId);
    }

}