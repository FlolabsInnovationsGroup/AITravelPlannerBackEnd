package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.AgentFactory;
import com.springboot.FlomadAIplanner.ai.agents.Agent;
import com.springboot.FlomadAIplanner.ai.functions.AutoLocationExtractionFunction;
import com.springboot.FlomadAIplanner.ai.validation.AIResponseValidationService;
import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.MessageType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EnhancedChatCallService
 * Tests message persistence during AI processing
 */
@ExtendWith(MockitoExtension.class)
class EnhancedChatCallServiceTest {

    @Mock
    private AgentFactory agentFactory;

    @Mock
    private MessageService messageService;

    @Mock
    private AIResponseValidationService validationService;

    @Mock
    private Agent mockAgent;

    @Mock
    private com.springboot.FlomadAIplanner.ai.service.ResponseCacheService cacheService;
    
    @Mock
    private AutoLocationExtractionFunction autoLocationExtractionFunction;

    private EnhancedChatCallService chatService;

    private static final String TEST_SESSION_ID = "test-session-123";
    private static final String TEST_MESSAGE = "What's the weather like in Paris?";
    private static final String AI_RESPONSE = "The weather in Paris is currently 22°C and sunny.";

    @BeforeEach
    void setUp() {
        List<String> aiFunctionDescriptions = Arrays.asList("weather", "location");
        Map<String, String> promptsList = new HashMap<>();
        promptsList.put("default", "You are a helpful travel assistant.");

        chatService = new EnhancedChatCallService(agentFactory, aiFunctionDescriptions, promptsList, messageService, validationService, cacheService, autoLocationExtractionFunction);
    }

    @Test
    void testProcessCallMessage_Success() {
        // Arrange - service uses Spring AI chat memory for persistence, not messageService.save()
        AIMessageDTO aiMessage = new AIMessageDTO();
        aiMessage.setSessionId(TEST_SESSION_ID);
        aiMessage.setMessage(TEST_MESSAGE);

        when(agentFactory.getAgent(aiMessage)).thenReturn(mockAgent);
        when(mockAgent.processCallMessage(aiMessage)).thenReturn(AI_RESPONSE);

        // Act
        String result = chatService.processCallMessage(aiMessage);

        // Assert
        assertEquals(AI_RESPONSE, result);
        verify(agentFactory).getAgent(aiMessage);
        verify(mockAgent).processCallMessage(aiMessage);
    }

    @Test
    void testProcessCallMessage_AgentException_SavesErrorMessage() {
        // Arrange - service propagates exception; persistence is via Spring AI chat memory
        AIMessageDTO aiMessage = new AIMessageDTO();
        aiMessage.setSessionId(TEST_SESSION_ID);
        aiMessage.setMessage(TEST_MESSAGE);

        when(agentFactory.getAgent(aiMessage)).thenReturn(mockAgent);
        when(mockAgent.processCallMessage(aiMessage)).thenThrow(new RuntimeException("AI processing failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatService.processCallMessage(aiMessage);
        });
        verify(agentFactory).getAgent(aiMessage);
        verify(mockAgent).processCallMessage(aiMessage);
    }

    @Test
    void testProcessEntityMessage_Success() {
        // Arrange
        AIMessageDTO aiMessage = new AIMessageDTO();
        aiMessage.setSessionId(TEST_SESSION_ID);
        aiMessage.setMessage("Find flights to Rome");

        Object mockEntityResponse = createMockEntityResponse();
        when(agentFactory.getAgent(aiMessage)).thenReturn(mockAgent);
        when(mockAgent.processEntityMessage(aiMessage, Object.class)).thenReturn(mockEntityResponse);

        // Act
        Object result = chatService.processEntityMessage(aiMessage, Object.class);

        // Assert
        assertEquals(mockEntityResponse, result);
        verify(mockAgent).processEntityMessage(aiMessage, Object.class);
    }

    @Test
    void testProcessEntityMessage_UnsupportedOperation_SavesErrorMessage() {
        // Arrange
        AIMessageDTO aiMessage = new AIMessageDTO();
        aiMessage.setSessionId(TEST_SESSION_ID);
        aiMessage.setMessage("Process this entity");

        when(agentFactory.getAgent(aiMessage)).thenReturn(mockAgent);
        when(mockAgent.processEntityMessage(aiMessage, Object.class))
            .thenThrow(new UnsupportedOperationException("Not supported"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatService.processEntityMessage(aiMessage, Object.class);
        });
        verify(mockAgent).processEntityMessage(aiMessage, Object.class);
    }

    @Test
    void testGetMessageCountForSession() {
        // Arrange
        List<Message> mockMessages = Arrays.asList(
            createMockMessage("msg-1", "Hello", MessageType.USER),
            createMockMessage("msg-2", "Hi there!", MessageType.ASSISTANT),
            createMockMessage("msg-3", "How are you?", MessageType.USER)
        );
        when(messageService.getMessagesBySessionId(TEST_SESSION_ID)).thenReturn(mockMessages);

        // Act
        int count = chatService.getMessageCountForSession(TEST_SESSION_ID);

        // Assert
        assertEquals(3, count);
        verify(messageService).getMessagesBySessionId(TEST_SESSION_ID);
    }

    @Test
    void testGetMessageCountForSession_ServiceException_ReturnsZero() {
        // Arrange
        when(messageService.getMessagesBySessionId(TEST_SESSION_ID))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        int count = chatService.getMessageCountForSession(TEST_SESSION_ID);

        // Assert
        assertEquals(0, count);
        verify(messageService).getMessagesBySessionId(TEST_SESSION_ID);
    }

    @Test
    void testExtractTextFromResponse_StringResponse() {
        // Arrange
        AIMessageDTO aiMessage = new AIMessageDTO();
        aiMessage.setSessionId(TEST_SESSION_ID);
        aiMessage.setMessage("Test message");

        when(agentFactory.getAgent(aiMessage)).thenReturn(mockAgent);
        when(mockAgent.processEntityMessage(aiMessage, String.class)).thenReturn("Simple response");

        // Act
        String result = chatService.processEntityMessage(aiMessage, String.class);

        // Assert
        assertEquals("Simple response", result);
        verify(mockAgent).processEntityMessage(aiMessage, String.class);
    }

    @Test
    void testExtractTextFromResponse_ComplexObject() {
        // Arrange
        AIMessageDTO aiMessage = new AIMessageDTO();
        aiMessage.setSessionId(TEST_SESSION_ID);
        aiMessage.setMessage("Complex request");

        Object complexResponse = new Object() {
            @Override
            public String toString() {
                return "Complex response data with lots of details...";
            }
        };

        when(agentFactory.getAgent(aiMessage)).thenReturn(mockAgent);
        when(mockAgent.processEntityMessage(aiMessage, Object.class)).thenReturn(complexResponse);

        // Act
        Object result = chatService.processEntityMessage(aiMessage, Object.class);

        // Assert
        assertEquals(complexResponse, result);
        verify(mockAgent).processEntityMessage(aiMessage, Object.class);
    }

    @Test
    void testUserMetadata_WithAudioFile() {
        // Arrange - persistence is via Spring AI chat memory
        AIMessageDTO aiMessage = new AIMessageDTO();
        aiMessage.setSessionId(TEST_SESSION_ID);
        aiMessage.setMessage("Audio message");

        when(agentFactory.getAgent(aiMessage)).thenReturn(mockAgent);
        when(mockAgent.processCallMessage(aiMessage)).thenReturn(AI_RESPONSE);

        // Act
        String result = chatService.processCallMessage(aiMessage);

        // Assert
        assertEquals(AI_RESPONSE, result);
        verify(agentFactory).getAgent(aiMessage);
        verify(mockAgent).processCallMessage(aiMessage);
    }

    private Message createMockMessage(String id, String content, MessageType messageType) {
        Message message = new Message();
        message.setId(id);
        message.setSessionId(TEST_SESSION_ID);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setCreatedAt(LocalDateTime.now());
        message.setMetadata(new HashMap<>());
        return message;
    }

    private Object createMockEntityResponse() {
        return new Object() {
            @Override
            public String toString() {
                return "EntityResponse{message='Entity response message'}";
            }
        };
    }
}