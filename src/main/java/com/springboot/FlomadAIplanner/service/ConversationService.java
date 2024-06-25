package com.springboot.FlomadAIplanner.service;

import com.springboot.FlomadAIplanner.DatabaseManagement.ConversationRepository;
import com.springboot.FlomadAIplanner.DatabaseManagement.NewMessage;
import com.springboot.FlomadAIplanner.DatabaseManagement.Speaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

////////////////////////////////////////////

/**
 * @class
 * @description This is a Jpa object, where you can define datebase operation functions like findByUserId, Jpa object will read the function
 * name and infer the intent of coder. This is an interface with the database. This service operated the message table.
 */
//////////////////////////////////////////
@Slf4j
@Service
public class ConversationService {
    @Autowired
    private ConversationRepository repository;

    public NewMessage saveMessage(String id, String message, LocalDateTime timestamp, Speaker speaker) {
        log.info("Saving entity: {}", message);
        NewMessage messages = new NewMessage();
        messages.setSpeaker(speaker);
        messages.setContent(message);
        messages.setTimestamp(timestamp);
        messages.setSessionId(id);

        return repository.save(messages);
    }

    public List<NewMessage> getMessagesBySessionId(String sessionId) {
        log.info("Finding entity for session: {}", sessionId);
        return repository.findBySessionIdOrderByTimestampAsc(sessionId);
    }
}
