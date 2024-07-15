package com.springboot.FlomadAIplanner.service.model;

import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.model.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @class
 * @description This is a Jpa object, where you can define datebase operation functions like findByUserId, Jpa object will read the function
 * name and infer the intent of coder. This is an interface with the database. This service operated the message table.
 */
@Slf4j
@Service
public class MessageService {
    private final MessageRepository repository;

    public MessageService(MessageRepository repository) {
        this.repository = repository;
    }

    public Message save(Message message) {
        log.info("Saving entity: {}, message: {} sessionId: {}", Message.class, StringUtils.substring(message.getContent(), 0, 50), message.getSessionId());

        return repository.save(message);
    }

    public void saveAll(List<Message> messages) {
        log.info("Saving entities: {}", messages.size());
        repository.saveAll(messages);
    }

    public List<Message> getMessagesBySessionId(String sessionId) {
        log.info("Finding entity for session: {}", sessionId);
        return repository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    public List<Message> findAllBySessionId(String sessionId) {
        log.info("Finding entity for session: {}", sessionId);
        return repository.findAllBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    public void deleteBySessionId(String sessionId) {
        log.info("Deleting entity for session: {}", sessionId);
        repository.deleteBySessionId(sessionId);
    }

}
