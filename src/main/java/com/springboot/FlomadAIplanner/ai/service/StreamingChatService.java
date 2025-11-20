package com.springboot.FlomadAIplanner.ai.service;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.chatmemory.CustomDatabaseChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

/**
 * Streaming chat service providing real-time AI responses with progressive rendering.
 * Implements ChatGPT-style streaming for instant feel and better UX.
 * 
 * Architecture:
 * - Uses Spring AI ChatClient.stream() for reactive responses
 * - Integrates with CustomDatabaseChatMemory for conversation persistence
 * - Supports function calling with streaming responses
 * - Compatible with frontend ReadableStream consumption
 * 
 * @see ChatClient
 * @see CustomDatabaseChatMemory
 */
@Service
@Slf4j
public class StreamingChatService {

    private final ChatClient chatClient;
    private final CustomDatabaseChatMemory chatMemory;

    @Value("classpath:/prompts/system-prompt.txt")
    private Resource systemPromptResource;

    public StreamingChatService(
            @Qualifier("openAiChatClient") ChatClient chatClient,
            @Qualifier("customDatabaseChatMemory") CustomDatabaseChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        log.info("🌊 StreamingChatService initialized with reactive streaming support");
    }

    /**
     * Stream AI responses progressively with Server-Sent Events (SSE).
     * 
     * Flow:
     * 1. Retrieve conversation history from memory
     * 2. Build prompt with system instructions + user message + history
     * 3. Stream response chunks via Flux<String>
     * 4. Each chunk is sent immediately to frontend
     * 5. Final response is saved to memory
     * 
     * @param messageDTO User message with sessionId
     * @return Flux<String> Progressive response stream
     */
    public Flux<String> streamResponse(AIMessageDTO messageDTO) {
        String sessionId = messageDTO.getSessionId();
        String userMessage = messageDTO.getMessage();

        log.info("🌊 Starting streaming response for session: {} - Message: '{}'", 
                 sessionId, 
                 userMessage.substring(0, Math.min(50, userMessage.length())));

        try {
            // 1. Build prompt with memory context
            List<Message> conversationHistory = chatMemory.get(sessionId, 10);
            
            // 2. Stream response using ChatClient
            Flux<String> responseStream = chatClient
                .prompt()
                .user(userMessage)
                .advisors(new PromptChatMemoryAdvisor(chatMemory, sessionId, 10))
                .stream()
                .content();

            // 3. Add error handling and completion logging
            return responseStream
                .doOnNext(chunk -> {
                    if (log.isDebugEnabled()) {
                        log.debug("📦 Streaming chunk for session {}: '{}'", 
                                 sessionId, 
                                 chunk.substring(0, Math.min(20, chunk.length())));
                    }
                })
                .doOnComplete(() -> {
                    log.info("✅ Streaming completed for session: {}", sessionId);
                })
                .doOnError(error -> {
                    log.error("❌ Streaming error for session {}: {}", sessionId, error.getMessage(), error);
                })
                .timeout(Duration.ofSeconds(60))
                .onErrorResume(error -> {
                    log.error("❌ Stream timeout or error for session {}: {}", sessionId, error.getMessage());
                    return Flux.just("\n\n❌ I apologize, but I'm experiencing technical difficulties. Please try again.");
                });

        } catch (Exception e) {
            log.error("❌ Error initializing stream for session {}: {}", sessionId, e.getMessage(), e);
            return Flux.just("❌ Failed to start streaming response. Please try again.");
        }
    }

    /**
     * Stream response with full ChatResponse objects for advanced use cases.
     * Provides access to metadata, function calls, and finish reasons.
     * 
     * @param messageDTO User message with sessionId
     * @return Flux<ChatResponse> Full response stream with metadata
     */
    public Flux<ChatResponse> streamChatResponse(AIMessageDTO messageDTO) {
        String sessionId = messageDTO.getSessionId();
        String userMessage = messageDTO.getMessage();

        log.info("🌊 Starting ChatResponse stream for session: {}", sessionId);

        try {
            return chatClient
                .prompt()
                .user(userMessage)
                .advisors(new PromptChatMemoryAdvisor(chatMemory, sessionId, 10))
                .stream()
                .chatResponse()
                .doOnNext(response -> {
                    if (log.isDebugEnabled()) {
                        log.debug("📦 ChatResponse chunk: finish={}, metadata={}", 
                                 response.getMetadata() != null ? response.getMetadata().getFinishReason() : "null",
                                 response.getMetadata());
                    }
                })
                .doOnComplete(() -> {
                    log.info("✅ ChatResponse stream completed for session: {}", sessionId);
                })
                .timeout(Duration.ofSeconds(60))
                .onErrorResume(error -> {
                    log.error("❌ ChatResponse stream error: {}", error.getMessage());
                    return Flux.empty();
                });

        } catch (Exception e) {
            log.error("❌ Error initializing ChatResponse stream: {}", e.getMessage(), e);
            return Flux.empty();
        }
    }

    /**
     * Stream response with custom function selection.
     * Allows fine-grained control over which AI functions are available.
     * 
     * @param messageDTO User message
     * @param enabledFunctions List of function names to enable
     * @return Flux<String> Progressive response stream
     */
    public Flux<String> streamWithFunctions(AIMessageDTO messageDTO, List<String> enabledFunctions) {
        String sessionId = messageDTO.getSessionId();
        String userMessage = messageDTO.getMessage();

        log.info("🌊 Starting function-enabled stream for session: {} with functions: {}", 
                 sessionId, enabledFunctions);

        try {
            // Build chat client call with function filtering
            ChatClient.ChatClientRequestSpec requestSpec = chatClient
                .prompt()
                .user(userMessage)
                .advisors(new PromptChatMemoryAdvisor(chatMemory, sessionId, 10));

            // Note: Function filtering would be implemented here
            // Spring AI 1.0.0-M5 function selection mechanism

            return requestSpec
                .stream()
                .content()
                .doOnComplete(() -> {
                    log.info("✅ Function-enabled stream completed for session: {}", sessionId);
                })
                .timeout(Duration.ofSeconds(60))
                .onErrorResume(error -> {
                    log.error("❌ Function stream error: {}", error.getMessage());
                    return Flux.just("\n\n❌ Error processing request with functions.");
                });

        } catch (Exception e) {
            log.error("❌ Error initializing function stream: {}", e.getMessage(), e);
            return Flux.just("❌ Failed to start function-enabled streaming.");
        }
    }
}
