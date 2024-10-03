package com.springboot;

import com.springboot.FlomadAIplanner.model.entities.Session;
import com.springboot.FlomadAIplanner.model.repository.SessionRepository;
import com.springboot.FlomadAIplanner.service.model.GuestSessionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GuestSessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private GuestSessionService guestSessionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIncrementMessageCount_LessThanLimit() {
        String sessionId = "12345";

        // Mock the repository to return fewer than 5 sessions for the guest
        List<Session> guestSessions = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Session session = new Session();
            session.setSessionId(sessionId);
            session.setCreatedAt(LocalDateTime.now());
            guestSessions.add(session);
        }

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(guestSessions);

        // Call the method
        boolean result = guestSessionService.incrementMessageCount(sessionId);

        // Verify the result
        assertTrue(result);
    }

    @Test
    void testIncrementMessageCount_LimitReached() {
        String sessionId = "12345";

        // Mock the repository to return 5 sessions for the guest
        List<Session> guestSessions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Session session = new Session();
            session.setSessionId(sessionId);
            session.setCreatedAt(LocalDateTime.now());
            guestSessions.add(session);
        }

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(guestSessions);

        // Call the method
        boolean result = guestSessionService.incrementMessageCount(sessionId);

        // Verify the result
        assertFalse(result);
        // Verify that the promptUserToCreateAccount was called
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void testSave_NewGuestSession() {
        Session session = new Session();
        session.setUserId(null); // Guest user

        // Mock the repository save operation
        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        // Call the method
        Session result = guestSessionService.save(session);

        // Verify the result
        assertNotNull(result);
        verify(sessionRepository, times(1)).save(any(Session.class));
    }
}



