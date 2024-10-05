package com.springboot;

import com.springboot.FlomadAIplanner.controller.model.SessionController;
import com.springboot.FlomadAIplanner.model.entities.Session;
import com.springboot.FlomadAIplanner.service.model.SessionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class SessionControllerTest {

    @InjectMocks
    private SessionController sessionController;

    @Mock
    private SessionService sessionService;

    private String sessionId = "test-session-id";
    private String guestMessage = "Test message";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInteractWithSession_ExceedMessageLimit() {
        // Arrange
        Session session = new Session(sessionId, null, "Test Session", null, null);
        session.setMessageCount(6); // Set the message count to the limit
        when(sessionService.findById(sessionId)).thenReturn(session);
        when(sessionService.checkAndIncrementMessageCount(sessionId)).thenReturn(false);

        // Act
        ResponseEntity<?> response = sessionController.interactWithSession(sessionId, guestMessage);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Message limit reached. Please sign up or log in.", response.getBody());
    }

    @Test
    public void testInteractWithSession_WithinMessageLimit() {
        // Arrange
        Session session = new Session(sessionId, null, "Test Session", null, null);
        session.setMessageCount(4); // Set the message count below the limit
        when(sessionService.findById(sessionId)).thenReturn(session);
        when(sessionService.checkAndIncrementMessageCount(sessionId)).thenReturn(true);

        // Act
        ResponseEntity<?> response = sessionController.interactWithSession(sessionId, guestMessage);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Message processed successfully. Current message count: " + 4, response.getBody());
    }
}

