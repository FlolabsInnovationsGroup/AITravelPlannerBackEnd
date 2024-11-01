package com.sprintboot.FlomadPlanner.service.model;
import com.springboot.FlomadAIplanner.service.model.MessageService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import com.springboot.FlomadAIplanner.exceptions.validators.GuestMessageLimitReachedException;
import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.model.entities.Session;
import com.springboot.FlomadAIplanner.model.repository.MessageRepository;
import com.springboot.FlomadAIplanner.model.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.MessageType;

import java.util.Optional;

public class MessageServiceTest {
    private MessageService messageService;
    private MessageRepository messageRepository;
    private SessionRepository sessionRepository;

    @BeforeEach
    public void setup() {
        messageRepository = mock(MessageRepository.class);
        sessionRepository = mock(SessionRepository.class);
        messageService = new MessageService(messageRepository, sessionRepository);
    }

    @Test
    public void shouldThrowExceptionWhenGuestExceedsMessageLimit() {
        String sessionId = "guest-session-id";
        Message message = new Message();
        message.setSessionId(sessionId);

        // Set up the session as a guest by setting userType to "guest"
        Session session = new Session();
        session.setUserType("guest");

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(messageRepository.countBySessionId(sessionId)).thenReturn(5L);

        // Check if the exception is thrown when the guest exceeds the message limit
        assertThrows(GuestMessageLimitReachedException.class, () -> {
            messageService.save(message);
        });
    }
}


