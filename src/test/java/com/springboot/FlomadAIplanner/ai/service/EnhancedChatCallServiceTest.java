package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.factory.AgentFactory;
import com.springboot.FlomadAIplanner.ai.agents.Agent;
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
    private Agent mockAgent;

    private EnhancedChatCallService chatService;

    private static final String TEST_SESSION_ID = "test-session-123";
    private static final String TEST_MESSAGE = "What's the weather like in Paris?";
    private static final String AI_RESPONSE = "The weather in Paris is currently 22°C and sunny.";

    @BeforeEach
    void setUp() {
        List<String> aiFunctionDescriptions = Arrays.asList("weather", "location");
        Map<String, String> promptsList = new HashMap<>();
        promptsList.put("default", "You are a helpful travel assistant.");

        chatService = new EnhancedChatCallService(agentFactory, aiFunctionDescriptions, promptsList, messageService);
    }

    @Test
    void testProcessCallMessage_Success() {
        // Arrange
        AIMessageDTO aiMessage = new AIMessageDTO();
        aiMessage.setSessionId(TEST_SESSION_ID);
        aiMessage.setMessage(TEST_MESSAGE);

        Message savedUserMessage = createMockMessage("user-msg-1", TEST_MESSAGE, MessageType.USER);
        Message savedAiMessage = createMockMessage("ai-msg-1", AI_RESPONSE, MessageType.ASSISTANT);

        when(messageService.save(any(Message.class)))
            .thenReturn(savedUserMessage, savedAiMessage);
        when(agentFactory.getAgent(aiMessage)).thenReturn(mockAgent);
        when(mockAgent.processCallMessage(aiMessage)).thenReturn(AI_RESPONSE);

        // Act
        String result = chatService.processCallMessage(aiMessage);

        // Assert
        assertEquals(AI_RESPONSE, result);
        
        // Verify user message was saved
        verify(messageService, times(2)).save(argThat(message -> {
            if (message.getMessageType() == MessageType.USER) {
                return TEST_MESSAGE.equals(message.getContent()) && 
                       TEST_SESSION_ID.equals(message.getSessionId());
            } else if (message.getMessageType() == MessageType.ASSISTANT) {
                return AI_RESPONSE.equals(message.getContent()) && 
                       TEST_SESSION_ID.equals(message.getSessionId());
            }
            return false;
        }));
        
        verify(agentFactory).getAgent(aiMessage);
        verify(mockAgent).processCallMessage(aiMessage);
    }

    @Test
    void testProcessCallMessage_AgentException_SavesErrorMessage() {
        // Arrange
        AIMessageDTO aiMessage = new AIMessageDTO();
        aiMessage.setSessionId(TEST_SESSION_ID);
        aiMessage.setMessage(TEST_MESSAGE);

        Message savedUserMessage = createMockMessage("user-msg-1", TEST_MESSAGE, MessageType.USER);
        Message savedErrorMessage = createMockMessage("error-msg-1", "I apologize, but I encountered an error processing your request. Please try again.", MessageType.ASSISTANT);

        when(messageService.save(any(Message.class)))
            .thenReturn(savedUserMessage, savedErrorMessage);
        when(agentFactory.getAgent(aiMessage)).thenReturn(mockAgent);
        when(mockAgent.processCallMessage(aiMessage)).thenThrow(new RuntimeException("AI processing failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatService.processCallMessage(aiMessage);
        });

        // Verify error message was saved
        verify(messageService, times(2)).save(any(Message.class));
        verify(messageService).save(argThat(message -> 
            message.getMessageType() == MessageType.ASSISTANT &&
            message.getContent().contains("I apologize, but I encountered an error")
        ));
    }

    @Test
    void testProcessEntityMessage_Success() {
        // Arrange
        AIMessageDTO aiMessage = new AIMessageDTO();
        aiMessage.setSessionId(TEST_SESSION_ID);
        aiMessage.setMessage("Find flights to Rome");

        Object mockEntityResponse = createMockEntityResponse();
        Message savedUserMessage = createMockMessage("user-msg-1", "Find flights to Rome", MessageType.USER);
        Message savedAiMessage = createMockMessage("ai-msg-1", mockEntityResponse.toString(), MessageType.ASSISTANT);

        when(messageService.save(any(Message.class)))
            .thenReturn(savedUserMessage, savedAiMessage);
        when(agentFactory.getAgent(aiMessage)).thenReturn(mockAgent);
        when(mockAgent.processEntityMessage(aiMessage, Object.class)).thenReturn(mockEntityResponse);

        // Act
        Object result = chatService.processEntityMessage(aiMessage, Object.class);

        // Assert
        assertEquals(mockEntityResponse, result);
        verify(messageService, times(2)).save(any(Message.class));
        verify(mockAgent).processEntityMessage(aiMessage, Object.class);
    }

    @Test
    void testProcessEntityMessage_UnsupportedOperation_SavesErrorMessage() {
        // Arrange
        AIMessageDTO aiMessage = new AIMessageDTO();
        aiMessage.setSessionId(TEST_SESSION_ID);
        aiMessage.setMessage("Process this entity");

        Message savedUserMessage = createMockMessage("user-msg-1", "Process this entity", MessageType.USER);
        Message savedErrorMessage = createMockMessage("error-msg-1", "This type of request is not currently supported.", MessageType.ASSISTANT);

        when(messageService.save(any(Message.class)))
            .thenReturn(savedUserMessage, savedErrorMessage);
        when(agentFactory.getAgent(aiMessage)).thenReturn(mockAgent);
        when(mockAgent.processEntityMessage(aiMessage, Object.class))
            .thenThrow(new UnsupportedOperationException("Not supported"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatService.processEntityMessage(aiMessage, Object.class);
        });

        // Verify error message was saved
        verify(messageService, times(2)).save(any(Message.class));
        verify(messageService).save(argThat(message -> 
            message.getMessageType() == MessageType.ASSISTANT &&
            message.getContent().contains("not currently supported")
        ));
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

        when(messageService.save(any(Message.class)))
            .thenReturn(createMockMessage("user-msg", "Test message", MessageType.USER))
            .thenReturn(createMockMessage("ai-msg", "Simple response", MessageType.ASSISTANT));
        when(agentFactory.getAgent(aiMessage)).thenReturn(mockAgent);
        when(mockAgent.processEntityMessage(aiMessage, String.class)).thenReturn("Simple response");

        // Act
        String result = chatService.processEntityMessage(aiMessage, String.class);

        // Assert
        assertEquals("Simple response", result);
        verify(messageService).save(argThat(message -> 
            message.getMessageType() == MessageType.ASSISTANT &&
            "Simple response".equals(message.getContent())
        ));
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

        when(messageService.save(any(Message.class)))
            .thenReturn(createMockMessage("user-msg", "Complex request", MessageType.USER))
            .thenReturn(createMockMessage("ai-msg", complexResponse.toString(), MessageType.ASSISTANT));
        when(agentFactory.getAgent(aiMessage)).thenReturn(mockAgent);
        when(mockAgent.processEntityMessage(aiMessage, Object.class)).thenReturn(complexResponse);

        // Act
        Object result = chatService.processEntityMessage(aiMessage, Object.class);

        // Assert
        assertEquals(complexResponse, result);
        verify(messageService).save(argThat(message -> 
            message.getMessageType() == MessageType.ASSISTANT &&
            message.getContent().contains("Complex response data")
        ));
    }

    @Test
    void testUserMetadata_WithAudioFile() {
        // Arrange
        AIMessageDTO aiMessage = new AIMessageDTO();
        aiMessage.setSessionId(TEST_SESSION_ID);
        aiMessage.setMessage("Audio message");
        // Note: In real implementation, you would set aiMessage.setMedia(mockMultipartFile)

        Message savedUserMessage = createMockMessage("user-msg-1", "Audio message", MessageType.USER);
        Message savedAiMessage = createMockMessage("ai-msg-1", AI_RESPONSE, MessageType.ASSISTANT);

        when(messageService.save(any(Message.class)))
            .thenReturn(savedUserMessage, savedAiMessage);
        when(agentFactory.getAgent(aiMessage)).thenReturn(mockAgent);
        when(mockAgent.processCallMessage(aiMessage)).thenReturn(AI_RESPONSE);

        // Act
        chatService.processCallMessage(aiMessage);

        // Assert - Should save both user and assistant messages
        verify(messageService, times(2)).save(any(Message.class));
        
        // Verify user message metadata contains audio file info
        verify(messageService).save(argThat(message -> {
            if (message.getMessageType() == MessageType.USER) {
                return message.getMetadata() != null &&
                       message.getMetadata().containsKey("source") &&
                       "user_input".equals(message.getMetadata().get("source"));
            }
            return false; // Only check user messages for this test
        }));
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
            public String getMessage() {
                return "Entity response message";
            }
            
            @Override
            public String toString() {
                return "EntityResponse{message='Entity response message'}";
            }
        };
    }
}