package com.springboot.FlomadAIplanner.service.model;

import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.model.entities.Session;
import com.springboot.FlomadAIplanner.model.repository.MessageRepository;
import com.springboot.FlomadAIplanner.model.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService implements CrudService<Message, String> {
    private final MessageRepository messageRepository;
    private final SessionRepository sessionRepository;

    @Override
    public Message save(Message message) {
        log.info("Saving message: {}, content: {}, sessionId: {}", message, StringUtils.substring(message.getContent(), 0, 50), message.getSessionId());
        return messageRepository.save(message);
    }

    @Override
    public Message findById(String id) {
        log.info("Finding message by id: {}", id);
        return messageRepository.findById(id).orElse(null);
    }

    @Override
    public List<Message> findAll() {
        log.info("Finding all messages");
        return messageRepository.findAll();
    }

    @Override
    public Message update(String id, Message updatedMessage) {
        log.info("Updating message with id: {}", id);
        updatedMessage.setId(id);
        return messageRepository.save(updatedMessage);
    }

    @Override
    public void delete(String id) {
        log.info("Deleting message with id: {}", id);
        messageRepository.deleteById(id);
    }

    @SuppressWarnings("all")
    public List<Message> saveAll(List<Message> messages) {
        log.info("Saving messages: {}", messages.size());
        return messageRepository.saveAll(messages);
    }

    public List<Message> getMessagesBySessionId(String sessionId) {
        log.info("Finding messages for session: {}", sessionId);
        return messageRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    public long countMessagesBySessionId(String sessionId) {
        log.info("Count messages for session: {}", sessionId);
        return messageRepository.countMessagesBySessionId(sessionId);
    }

    public List<Message> getMessagesByUserId(String userId) {
        log.info("Finding messages for user: {}", userId);
        List<Session> sessions = sessionRepository.findByUserId(userId);
        List<String> sessionIds = sessions.stream().map(Session::getId).collect(Collectors.toList());
        return messageRepository.findBySessionIdInOrderByCreatedAtAsc(sessionIds);
    }

    public List<Message> getMessagesBySessionIdWithLimit(String sessionId, int limit) {
        log.info("Finding messages for session: {} with limit: {}", sessionId, limit);
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than zero");
        }
        Pageable pageable = PageRequest.of(0, limit);
        List<Message> messages = messageRepository.findBySessionIdOrderByCreatedAtDesc(sessionId, pageable);
        Collections.reverse(messages);
        return messages;
    }

    public void deleteBySessionId(String sessionId) {
        log.info("Deleting messages for session: {}", sessionId);
        messageRepository.deleteBySessionId(sessionId);
    }

    public Message addNewMessage(String sessionId, String content) {
        log.info("Adding new message to session: {}", sessionId);
        
        // Create a new message
        Message newMessage = new Message();
        newMessage.setSessionId(sessionId);
        newMessage.setContent(content);
        newMessage.setCreatedAt(LocalDateTime.now());

        // Save the new message
        return messageRepository.save(newMessage);
    }
}
