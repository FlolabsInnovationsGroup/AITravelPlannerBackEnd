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
    private static final int MAX_GUEST_MESSAGES = 5; // Set maximum guest interactions
    private final SessionRepository sessionRepository;

    public GuestSessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Saves a session for a guest user.
     * If the session is for a guest, it assigns a temporary session ID and checks for message count.
     *
     * @param session The session to be saved.
     * @return The saved session or null if the guest user exceeded the message limit.
     */
    public Session save(Session session) {
        log.info("Saving guest session: {}", session);

        // If no userId is provided, it's a guest session.
        if (session.getUserId() == null || session.getUserId().isEmpty()) {
            // Generate a unique session ID for the guest user.
            session.setSessionId(UUID.randomUUID().toString());
            session.setCreatedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
        }

        // If the guest has reached the maximum message limit, prevent saving the session.

        if (!incrementMessageCount(session.getSessionId())) {
            return null; // The session is not saved if the guest exceeded the interaction limit.

        }
        return sessionRepository.save(session);
    }

    /**
     * Increments the message count for a guest user session.
     * If the guest user exceeds the allowed message limit, prompts them to create an account.
     *
     * @param sessionId The ID of the guest session.
     * @return true if the message count is incremented successfully, false if the limit is exceeded.
     */
    public boolean incrementMessageCount(String sessionId) {
        List<Session> guestSessions = sessionRepository.findBySessionId(sessionId);

        if (guestSessions.isEmpty()) {
            Session newSession = new Session();
            newSession.setSessionId(sessionId);
            newSession.setCreatedAt(LocalDateTime.now());
            newSession.setUpdatedAt(LocalDateTime.now());
            newSession.setUserId(null); // Guest session has no userId
            guestSessions.add(newSession);
        }

        // Check if the message count limit is reached
        if (guestSessions.size() >= MAX_GUEST_MESSAGES) {
            promptUserToCreateAccount(sessionId);
            return false;
        }

        log.info("Incrementing message count for guest session:{}", sessionId);
        return true;
    }

    /**
     * Prompts the guest user to create an account after they reach the message interaction limit.
     *
     * @param sessionId The ID of the guest session.
     */
    private void promptUserToCreateAccount(String sessionId) {
        log.info("Prompting guest session {} to create an account after reaching interaction limit", sessionId);
        // Logic to notify the user to create an account
        String message = "Dear Guest, you've reached the maximum number of interactions allowed as a guest. Please create an account to continue enjoying our services.";
        log.info("Notification to Guest (Session ID: {}): {}", sessionId, message);
    }

    /**
     * Retrieves all guest sessions (i.e., sessions without an associated user ID).
     *
     * @return A list of DTOs for guest sessions.
     */
    public List<SessionDTO> findGuestSessions() {
        log.info("Finding all guest sessions");

        // Retrieve guest sessions (sessions without a userId) and sort them by creation date
        return sessionRepository.findByUserIdIsNull()
                .stream()
                .sorted(Comparator.comparing(Session::getCreatedAt).reversed())
                .map(session -> new SessionDTO(
                        session.getId(),
                        session.getUserId(),
                        session.getTitle(),
                        session.getCreatedAt(),
                        session.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }
}

