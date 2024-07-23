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

@Slf4j
@Service
public class SessionService implements CrudService<Session, String> {
    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Session save(Session session) {
        log.info("Saving session: {}", session);
        return sessionRepository.save(session);
    }

    @Override
    public Session findById(String id) {
        log.info("Finding session by id: {}", id);
        return sessionRepository.findById(id).orElse(null);
    }

    @Override
    public List<Session> findAll() {
        log.info("Finding all sessions");
        return sessionRepository.findAll();
    }

    @Override
    public Session update(String id, Session updatedSession) {
        log.info("Updating session: {}", id);

        String title = updatedSession.getTitle();
        String userId = updatedSession.getUserId();

        Session session = sessionRepository.findByIdAndUserId(id, userId);
        if (session != null) {
            session.setTitle(title);
            session.setUpdatedAt(LocalDateTime.now());
            return sessionRepository.save(session);
        } else {
            log.error("Session not found");
            throw new RuntimeException("Session not found");
        }
    }

    @Override
    public void delete(String id) {
        log.info("Deleting session with id: {}", id);
        sessionRepository.deleteById(id);
    }

    public List<SessionDTO> findSessionIdsByUserId(String userId) {
        log.info("Finding sessions for user: {}", userId);
        return sessionRepository.findByUserId(userId)
                .stream()
                .sorted(Comparator.comparing(Session::getCreatedAt).reversed())
                .map(session -> new SessionDTO(session.getId(), session.getUserId(), session.getTitle(), session.getCreatedAt(), session.getUpdatedAt()))
                .collect(Collectors.toList());
    }

}
