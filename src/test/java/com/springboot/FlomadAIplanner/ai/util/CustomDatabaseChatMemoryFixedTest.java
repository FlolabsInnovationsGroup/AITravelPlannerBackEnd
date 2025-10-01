package com.springboot.FlomadAIplanner.ai.util;

import com.springboot.FlomadAIplanner.converter.ChatMemoryMessageConverter;
import com.springboot.FlomadAIplanner.converter.ChatMemoryMessageRevertConverter;
import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomDatabaseChatMemoryFixed
 * Tests the database-backed chat memory implementation
 */
@ExtendWith(MockitoExtension.class)
class CustomDatabaseChatMemoryFixedTest {

    @Mock
    private MessageService messageService;

    @Mock
    private ChatMemoryMessageConverter messageConverter;

    @Mock
    private ChatMemoryMessageRevertConverter messageRevertConverter;

    private CustomDatabaseChatMemoryFixed chatMemory;

    private static final String TEST_CONVERSATION_ID = "test-session-123";
    private static final String DEFAULT_CONVERSATION_ID = "default";

    @BeforeEach
    void setUp() {
        chatMemory = new CustomDatabaseChatMemoryFixed(messageService, messageConverter, messageRevertConverter);
    }

    @Test
    void testAddMessages_Success() {
        // Arrange
        List<org.springframework.ai.chat.messages.Message> springMessages = Arrays.asList(
            new UserMessage("Hello, how are you?"),
            new AssistantMessage("I'm doing well, thank you!")
        );

        Message mockUserEntity = createMockMessageEntity("user-msg-1", "Hello, how are you?", MessageType.USER);
        Message mockAssistantEntity = createMockMessageEntity("assistant-msg-1", "I'm doing well, thank you!", MessageType.ASSISTANT);

        when(messageConverter.convert(any(org.springframework.ai.chat.messages.Message.class)))
            .thenReturn(mockUserEntity, mockAssistantEntity);
        when(messageService.save(any(Message.class)))
            .thenReturn(mockUserEntity, mockAssistantEntity);
        when(messageService.getMessagesBySessionId(TEST_CONVERSATION_ID))
            .thenReturn(Arrays.asList(mockUserEntity, mockAssistantEntity));

        // Act
        chatMemory.add(TEST_CONVERSATION_ID, springMessages);

        // Assert
        verify(messageConverter, times(2)).convert(any(org.springframework.ai.chat.messages.Message.class));
        verify(messageService, times(2)).save(any(Message.class));
        verify(messageService).getMessagesBySessionId(TEST_CONVERSATION_ID);
    }

    @Test
    void testAddMessages_EmptyList() {
        // Arrange
        List<org.springframework.ai.chat.messages.Message> emptyMessages = Collections.emptyList();

        // Act
        chatMemory.add(TEST_CONVERSATION_ID, emptyMessages);

        // Assert
        verify(messageConverter, never()).convert(any());
        verify(messageService, never()).save(any());
    }

    @Test
    void testAddMessages_DefaultConversation_Skipped() {
        // Arrange
        List<org.springframework.ai.chat.messages.Message> messages = Arrays.asList(
            new UserMessage("This should be skipped")
        );

        // Act
        chatMemory.add(DEFAULT_CONVERSATION_ID, messages);

        // Assert
        verify(messageConverter, never()).convert(any());
        verify(messageService, never()).save(any());
    }

    @Test
    void testGetMessages_Success() {
        // Arrange
        Message mockEntity1 = createMockMessageEntity("msg-1", "Hello", MessageType.USER);
        Message mockEntity2 = createMockMessageEntity("msg-2", "Hi there!", MessageType.ASSISTANT);
        List<Message> mockEntities = Arrays.asList(mockEntity1, mockEntity2);

        org.springframework.ai.chat.messages.Message mockSpringMsg1 = new UserMessage("Hello");
        org.springframework.ai.chat.messages.Message mockSpringMsg2 = new AssistantMessage("Hi there!");

        when(messageService.getMessagesBySessionId(TEST_CONVERSATION_ID)).thenReturn(mockEntities);
        when(messageRevertConverter.convert(mockEntity1)).thenReturn(mockSpringMsg1);
        when(messageRevertConverter.convert(mockEntity2)).thenReturn(mockSpringMsg2);

        // Act
        List<org.springframework.ai.chat.messages.Message> result = chatMemory.get(TEST_CONVERSATION_ID, 10);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Hello", result.get(0).getText());
        assertEquals("Hi there!", result.get(1).getText());
        verify(messageService).getMessagesBySessionId(TEST_CONVERSATION_ID);
        verify(messageRevertConverter, times(2)).convert(any(Message.class));
    }

    @Test
    void testGetMessages_LastNLimit() {
        // Arrange - 5 messages but only want last 3
        List<Message> allMessages = Arrays.asList(
            createMockMessageEntity("msg-1", "Message 1", MessageType.USER),
            createMockMessageEntity("msg-2", "Message 2", MessageType.ASSISTANT),
            createMockMessageEntity("msg-3", "Message 3", MessageType.USER),
            createMockMessageEntity("msg-4", "Message 4", MessageType.ASSISTANT),
            createMockMessageEntity("msg-5", "Message 5", MessageType.USER)
        );

        when(messageService.getMessagesBySessionId(TEST_CONVERSATION_ID)).thenReturn(allMessages);
        when(messageRevertConverter.convert(any(Message.class)))
            .thenReturn(new UserMessage("Converted message"));

        // Act
        List<org.springframework.ai.chat.messages.Message> result = chatMemory.get(TEST_CONVERSATION_ID, 3);

        // Assert
        assertEquals(3, result.size());
        verify(messageRevertConverter, times(3)).convert(any(Message.class));
    }

    @Test
    void testGetMessages_DefaultConversation_ReturnsEmpty() {
        // Act
        List<org.springframework.ai.chat.messages.Message> result = chatMemory.get(DEFAULT_CONVERSATION_ID, 10);

        // Assert
        assertTrue(result.isEmpty());
        verify(messageService, never()).getMessagesBySessionId(any());
    }

    @Test
    void testGetMessages_ZeroOrNegativeLimit_ReturnsEmpty() {
        // Act & Assert
        assertTrue(chatMemory.get(TEST_CONVERSATION_ID, 0).isEmpty());
        assertTrue(chatMemory.get(TEST_CONVERSATION_ID, -5).isEmpty());
        verify(messageService, never()).getMessagesBySessionId(any());
    }

    @Test
    void testClearConversation_Success() {
        // Arrange
        Message mockMsg1 = createMockMessageEntity("msg-1", "Hello", MessageType.USER);
        Message mockMsg2 = createMockMessageEntity("msg-2", "Hi", MessageType.ASSISTANT);
        List<Message> mockMessages = Arrays.asList(mockMsg1, mockMsg2);

        when(messageService.getMessagesBySessionId(TEST_CONVERSATION_ID)).thenReturn(mockMessages);

        // Act
        chatMemory.clear(TEST_CONVERSATION_ID);

        // Assert
        verify(messageService).getMessagesBySessionId(TEST_CONVERSATION_ID);
        verify(messageService).delete("msg-1");
        verify(messageService).delete("msg-2");
    }

    @Test
    void testClearConversation_DefaultConversation_Skipped() {
        // Act
        chatMemory.clear(DEFAULT_CONVERSATION_ID);

        // Assert
        verify(messageService, never()).getMessagesBySessionId(any());
        verify(messageService, never()).delete(any());
    }

    @Test
    void testMessageTruncation_LongContent() {
        // Arrange - Create a very long message that should be truncated
        String longMessage = "A".repeat(5000); // Much longer than 800 tokens * 4 chars = 3200 chars
        List<org.springframework.ai.chat.messages.Message> messages = Arrays.asList(
            new UserMessage(longMessage)
        );

        Message mockEntity = createMockMessageEntity("msg-1", longMessage, MessageType.USER);
        when(messageConverter.convert(any())).thenReturn(mockEntity);
        when(messageService.save(any(Message.class))).thenReturn(mockEntity);
        when(messageService.getMessagesBySessionId(TEST_CONVERSATION_ID)).thenReturn(Arrays.asList(mockEntity));

        // Act
        chatMemory.add(TEST_CONVERSATION_ID, messages);

        // Assert
        verify(messageService).save(argThat(message -> 
            message.getContent().contains("... [truncated]") && 
            message.getContent().length() <= 3200 + 20 // Extra for truncation text
        ));
    }

    @Test
    void testGetMessageCount() {
        // Arrange
        List<Message> mockMessages = Arrays.asList(
            createMockMessageEntity("msg-1", "Hello", MessageType.USER),
            createMockMessageEntity("msg-2", "Hi", MessageType.ASSISTANT)
        );
        when(messageService.getMessagesBySessionId(TEST_CONVERSATION_ID)).thenReturn(mockMessages);

        // Act
        int count = chatMemory.getMessageCount(TEST_CONVERSATION_ID);

        // Assert
        assertEquals(2, count);
        verify(messageService).getMessagesBySessionId(TEST_CONVERSATION_ID);
    }

    @Test
    void testErrorHandling_ServiceException() {
        // Arrange
        List<org.springframework.ai.chat.messages.Message> messages = Arrays.asList(
            new UserMessage("Test message")
        );
        when(messageConverter.convert(any())).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatMemory.add(TEST_CONVERSATION_ID, messages);
        });
    }

    @Test
    void testGetMessages_ServiceException_ReturnsEmpty() {
        // Arrange
        when(messageService.getMessagesBySessionId(TEST_CONVERSATION_ID))
            .thenThrow(new RuntimeException("Database connection failed"));

        // Act
        List<org.springframework.ai.chat.messages.Message> result = chatMemory.get(TEST_CONVERSATION_ID, 10);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testCleanupOldMessages_ExceedsLimit() {
        // Arrange - Simulate having more than maxMessages (default 100)
        List<Message> manyMessages = new ArrayList<>();
        for (int i = 0; i < 120; i++) {
            manyMessages.add(createMockMessageEntity("msg-" + i, "Message " + i, MessageType.USER));
        }

        List<org.springframework.ai.chat.messages.Message> newMessages = Arrays.asList(
            new UserMessage("New message")
        );

        Message mockNewEntity = createMockMessageEntity("new-msg", "New message", MessageType.USER);
        when(messageConverter.convert(any())).thenReturn(mockNewEntity);
        when(messageService.save(any())).thenReturn(mockNewEntity);
        when(messageService.getMessagesBySessionId(TEST_CONVERSATION_ID))
            .thenReturn(manyMessages)
            .thenReturn(manyMessages); // Called twice - once for saving, once for cleanup

        // Act
        chatMemory.add(TEST_CONVERSATION_ID, newMessages);

        // Assert
        verify(messageService, times(20)).delete(startsWith("msg-")); // Should delete oldest 20 messages
    }

    private Message createMockMessageEntity(String id, String content, MessageType messageType) {
        Message message = new Message();
        message.setId(id);
        message.setSessionId(TEST_CONVERSATION_ID);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setCreatedAt(LocalDateTime.now());
        message.setMetadata(new HashMap<>());
        return message;
    }
}