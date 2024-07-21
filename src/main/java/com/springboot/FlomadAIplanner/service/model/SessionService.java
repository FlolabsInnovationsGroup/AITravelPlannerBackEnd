package com.springboot.FlomadAIplanner.service.model;

import com.springboot.FlomadAIplanner.dto.SessionDTO;
import com.springboot.FlomadAIplanner.model.entities.Session;
import com.springboot.FlomadAIplanner.model.repository.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @class
 * @description This is a Jpa object, where you can define datebase operation functions like findByUserId, Jpa object will read the function
 * name and infer the intent of coder. This is an interface with the database. This service operated the session table.
 */
@Slf4j
@Service
public class SessionService {
    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public List<SessionDTO> findSessionIdsByUserId(String userId) {
        log.info("Finding entity for user: {}", userId);
        return sessionRepository.findByUserId(userId)
                .stream()
                .sorted(Comparator.comparing(Session::getCreatedAt).reversed())
                .map(session -> new SessionDTO(session.getSessionId(), session.getUserId(), session.getTitle(), session.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public boolean existsByUserId(String userId) {
        return sessionRepository.existsByUserId(userId);
    }

    public boolean existsBySessionIdAndUserId(String sessionId, String userId) {
        return sessionRepository.existsBySessionIdAndUserId(sessionId, userId);
    }

    public Session saveSession(Session session) {
        log.info("Saving entity: {}", session);
        return sessionRepository.save(session);
    }

    public boolean existsById(String id) {
        return sessionRepository.existsById(id);
    }

    public Session updateSession(String sessionId, String userId, String title, LocalDateTime timestamp) {
        log.info("Updating entity: {}", sessionId);
        Session session = sessionRepository.findBySessionIdAndUserId(sessionId, userId);
        if (session != null) {
            session.setTitle(title);
            session.setUpdatedAt(timestamp);
            return sessionRepository.save(session);
        } else {
            log.error("Entity not found");
            throw new RuntimeException("Session not found");
        }
    }

    public Session updateSessionTitle(String sessionId, String userId, String title) {
        log.info("Updating entity session ID: {}", sessionId);
        log.info("Updating entity uer ID: {}", userId);
        Session session = sessionRepository.findBySessionIdAndUserId(sessionId, userId);
        if (session != null) {
            session.setTitle(title);
            return sessionRepository.save(session);
        } else {
            log.error("Entity not found");
            throw new RuntimeException("Session not found");
        }
    }
}
