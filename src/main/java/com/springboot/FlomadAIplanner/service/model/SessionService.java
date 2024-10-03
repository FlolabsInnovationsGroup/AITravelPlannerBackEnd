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
    private final GuestSessionService guestSessionService;

    public SessionService(SessionRepository sessionRepository, GuestSessionService guestSessionService) {
        this.sessionRepository = sessionRepository;
        this.guestSessionService = guestSessionService;
    }

    @Override
    public Session save(Session session) {
        log.info("Saving session: {}", session);

        // If the session does not have a userId, handle as a guest session
        if (session.getUserId() == null || session.getUserId().isEmpty()) {
            return guestSessionService.save(session);
        }

        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    @Override
    public Session findById(String id) {
        return sessionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Session not found"));
    }

    @Override
    public List<Session> findAll() {
        return sessionRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Session::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<SessionDTO> findSessionIdsByUserId(String userId) {
        return sessionRepository.findByUserId(userId)
                .stream()
                .map(session -> new SessionDTO(session.getId(), session.getUserId(), session.getTitle(), session.getCreatedAt(), session.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public Session update(String id, Session updatedSession) {
        Session existingSession = findById(id);
        existingSession.setTitle(updatedSession.getTitle());
        existingSession.setUpdatedAt(LocalDateTime.now());
        return sessionRepository.save(existingSession);
    }

    @Override
    public void delete(String id) {
        sessionRepository.deleteById(id);
    }
}

