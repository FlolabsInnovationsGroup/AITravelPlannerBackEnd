package com.springboot.FlomadAIplanner.service.model;

import com.springboot.FlomadAIplanner.dto.SessionDTO;
import com.springboot.FlomadAIplanner.exceptions.validators.GuestMessageLimitReachedException;
import com.springboot.FlomadAIplanner.model.entities.Session;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.model.repository.SessionRepository;
import com.springboot.FlomadAIplanner.util.CurrentUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SessionService implements CrudService<Session, String> {

    private final SessionRepository sessionRepository;
    private final MessageService messageService;
    private final UserService userService;
    private final CurrentUserUtil currentUserUtil;
    @Value("${application.guest-message-limit}")
    private int GUEST_MESSAGE_LIMIT;

    public SessionService(SessionRepository sessionRepository, MessageService messageService, UserService userService, CurrentUserUtil currentUserUtil) {
        this.sessionRepository = sessionRepository;
        this.messageService = messageService;
        this.userService = userService;
        this.currentUserUtil = currentUserUtil;
    }

    @Override
    public Session save(Session session) {
        log.info("Saving session: {}", session);
        
        // If userId is not set, try to get from security context
        if (session.getUserId() == null) {
            User user = currentUserUtil.getCurrentUserFromSecurityContext();
            if (user != null) {
                session.setUserId(user.getId());
                log.info("Set userId from security context: {}", user.getId());
            } else {
                log.warn("⚠️ No authenticated user in security context. Session will be saved without userId.");
                // Don't throw exception - allow session creation for guest users
                // The userId will be set when the user authenticates with their JWT token
            }
        }
        
        // ✅ FIX: Automatically set timestamps if not already set
        LocalDateTime now = LocalDateTime.now();
        if (session.getCreatedAt() == null) {
            session.setCreatedAt(now);
            log.info("Set createdAt timestamp: {}", now);
        }
        if (session.getUpdatedAt() == null) {
            session.setUpdatedAt(now);
            log.info("Set updatedAt timestamp: {}", now);
        }
        
        Session savedSession = sessionRepository.save(session);
        log.info("✅ Session saved with ID: {}", savedSession.getId());
        return savedSession;
    }

    public void checkGuestUserLimitations(String sessionId, int newMessagesAmount) {
        Session session = sessionRepository.findById(sessionId).orElse(null);
        assert session != null;
        User user = userService.findById(session.getUserId());
        if (userService.isGuest(user)) { // Guest session
            long messageCount = messageService.countMessagesByUserId(user.getId(), MessageType.USER) + newMessagesAmount;
            if (messageCount > GUEST_MESSAGE_LIMIT) {
                throw new GuestMessageLimitReachedException("Guest session has reached the message limit of " + GUEST_MESSAGE_LIMIT);
            }
        }
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
        String userId = updatedSession.getUserId(); // Use userId from the session object first
        
        // Fallback to current user from security context if userId not provided
        if (userId == null || userId.isEmpty()) {
            try {
                User currentUser = currentUserUtil.getCurrentUserFromSecurityContext();
                if (currentUser != null) {
                    userId = currentUser.getId();
                } else {
                    log.error("No user ID provided and no authenticated user found for session update: {}", id);
                    throw new RuntimeException("No user context available for session update");
                }
            } catch (Exception e) {
                log.error("Failed to get current user for session update: {}", e.getMessage());
                throw new RuntimeException("User authentication required for session update");
            }
        }

        Session session = sessionRepository.findByIdAndUserId(id, userId);
        if (session != null) {
            session.setTitle(title);
            session.setUpdatedAt(LocalDateTime.now());
            
            // ✅ FIX: Ensure createdAt is preserved (should never be null for existing sessions)
            if (session.getCreatedAt() == null) {
                log.warn("⚠️ Session {} has null createdAt, setting to updatedAt", id);
                session.setCreatedAt(LocalDateTime.now());
            }
            
            return sessionRepository.save(session);
        } else {
            log.error("Session not found for id: {} and userId: {}", id, userId);
            throw new RuntimeException("Session not found");
        }
    }

    @Override
    public void delete(String id) {
        log.info("Deleting session with id: {}", id);
        sessionRepository.deleteById(id);
    }

    public void deleteAll() {
        log.info("Deleting all sessions");
        sessionRepository.deleteAll();
    }

    public List<Session> findSessionsByUserId(String userId) {
        log.info("Finding sessions for user: {}", userId);
        return sessionRepository.findByUserId(userId)
                .stream()
                .sorted(Comparator.comparing(Session::getCreatedAt).reversed())
                .collect(Collectors.toList());
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
