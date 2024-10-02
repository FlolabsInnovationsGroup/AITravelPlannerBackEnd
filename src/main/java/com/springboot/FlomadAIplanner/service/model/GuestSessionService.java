package com.springboot.FlomadAIplanner.service.model;

import com.springboot.FlomadAIplanner.dto.SessionDTO;
import com.springboot.FlomadAIplanner.model.entities.Session;
import com.springboot.FlomadAIplanner.model.repository.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GuestSessionService {
    private final SessionRepository sessionRepository;
    private static final int MAX_GUEST_INTERACTIONS = 5; // Set maximum guest interactions

    public GuestSessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Session save(Session session) {
        log.info("Saving guest session: {}", session);

        // Generate a temporary session ID for the guest user
        if (session.getUserId() == null || session.getUserId().isEmpty()) {
            session.setSessionId(UUID.randomUUID().toString());
            session.setCreatedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
        }

        if (getGuestInteractionsCount(session.getSessionId()) >= MAX_GUEST_INTERACTIONS) {
            promptUserToCreateAccount(session.getSessionId());
        }

        return sessionRepository.save(session);
    }

    private int getGuestInteractionsCount(String sessionId) {
        // Logic to retrieve the interaction count for the guest user
        // TODO: Replace with real implementation
        return 5; // Example value
    }

    private void promptUserToCreateAccount(String sessionId) {
        log.info("Prompting guest session {} to create an account after reaching interaction limit", sessionId);
        // Logic to notify the user to create an account
    }

    public List<SessionDTO> findGuestSessions() {
        log.info("Finding all guest sessions");
        return sessionRepository.findByUserIdIsNull()
                .stream()
                .sorted(Comparator.comparing(Session::getCreatedAt).reversed())
                .map(session -> new SessionDTO(session.getId(), session.getUserId(), session.getTitle(), session.getCreatedAt(), session.getUpdatedAt()))
                .collect(Collectors.toList());
    }
}

