package com.springboot.FlomadAIplanner.service.DataPersistance;

import com.springboot.FlomadAIplanner.DatabaseManagement.NewSession;
import com.springboot.FlomadAIplanner.DatabaseManagement.SessionDto;
import com.springboot.FlomadAIplanner.DatabaseManagement.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

////////////////////////////////////////////

/**
 * @class
 * @description This is a Jpa object, where you can define datebase operation functions like findByUserId, Jpa object will read the function
 * name and infer the intent of coder. This is an interface with the database. This service operated the session table.
 */
//////////////////////////////////////////
@Slf4j
@Service
public class SessionService {
    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public List<SessionDto> findSessionIdsByUserId(String userId) {
        log.info("Finding entity for user: {}", userId);
        return sessionRepository.findByUserId(userId)
                .stream()
                .sorted(Comparator.comparing(NewSession::getTimestamp).reversed())
                .map(session -> new SessionDto(session.getSessionId(), session.getUserId(), session.getTitle(), session.getTimestamp()))
                .collect(Collectors.toList());
    }

    public NewSession saveSession(NewSession session) {
        log.info("Saving entity: {}", session);
        return sessionRepository.save(session);
    }

    public NewSession updateSession(String sessionId, String userId, String title, LocalDateTime timestamp) {
        log.info("Updating entity: {}", sessionId);
        NewSession session = sessionRepository.findBySessionIdAndUserId(sessionId, userId);
        if (session != null) {
            session.setTitle(title);
            session.setTimestamp(timestamp);
            return sessionRepository.save(session);
        } else {
            log.error("Entity not found");
            throw new RuntimeException("Session not found");
        }
    }

    public NewSession updateSessionTitle(String sessionId, String userId, String title) {
        log.info("Updating entity: {}", sessionId);
        NewSession session = sessionRepository.findBySessionIdAndUserId(sessionId, userId);
        if (session != null) {
            session.setTitle(title);
            return sessionRepository.save(session);
        } else {
            log.error("Entity not found");
            throw new RuntimeException("Session not found");
        }
    }
}
