package com.springboot.FlomadAIplanner.service.model;

import com.springboot.FlomadAIplanner.exceptions.validators.GuestMessageLimitReachedException;
import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.model.entities.Session;
import com.springboot.FlomadAIplanner.model.repository.MessageRepository;
import com.springboot.FlomadAIplanner.model.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService implements CrudService<Message, String> {
    private final MessageRepository messageRepository;
    private final SessionRepository sessionRepository;

    private static final int GUEST_MESSAGE_LIMIT = 5;

    @Override
    public Message save(Message message) {
        // Check if user is a guest and has exceeded message limit
        if (isGuestUser(message.getSessionId()) && hasReachedMessageLimit(message.getSessionId())) {
            throw new GuestMessageLimitReachedException("Guest users have reached the maximum allowed messages (5). Please log in or sign up.");
        }
        log.info("Saving message: {}, content: {}, sessionId: {}", message, StringUtils.substring(message.getContent(), 0, 50), message.getSessionId());
        return messageRepository.save(message);
    }

    //Method to check if a session belongs ot a guest user
    private boolean isGuestUser(String sessionId) {
        Session session = sessionRepository.findById(sessionId).orElse(null);
        return session != null && session.isGuest(); //Assuming Session has an isGuest()method
    }

    //Method to check if the guest user has reached the message limit
    private boolean hasReachedMessageLimit(String sessionId) {
        long messageCount = messageRepository.countBySessionId(sessionId);
        log.info("Message count for session {} is : {}", sessionId, messageCount);
        return messageCount >= GUEST_MESSAGE_LIMIT;
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

    private long countBySessionIdIn(Collection<String> sessionIds, MessageType messageType) {
        log.info("Counting messages for session IDs: {} and messageType: {}", sessionIds, messageType);
        long count = messageRepository.countBySessionIdInAndMessageType(sessionIds, messageType);
        log.info("Counted {} messages for sessions: {}", count, sessionIds);
        return count;
    }

    public long countMessagesByUserId(String userId, MessageType messageType) {
        log.info("Counting messages for user: {}", userId);
        List<Session> sessions = sessionRepository.findSessionByUserId(userId);
        List<String> sessionIds = sessions.stream().map(Session::getId).collect(Collectors.toList());
        long count = this.countBySessionIdIn(sessionIds, messageType);
        log.info("Counted {} messages for user: {}", count, userId);
        return count;
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

}
