package com.springboot.FlomadAIplanner.ai.controller;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageResponse;
import com.springboot.FlomadAIplanner.ai.service.EnhancedChatCallService;
import com.springboot.FlomadAIplanner.ai.service.FastAIService;
import com.springboot.FlomadAIplanner.ai.service.LightweightAIService;
import com.springboot.FlomadAIplanner.ai.service.StreamingChatService;
import com.springboot.FlomadAIplanner.ai.agents.audio.AudioTranscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;


@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final EnhancedChatCallService enhancedChatCallService;
    private final FastAIService fastAIService;
    private final LightweightAIService lightweightAIService;
    private final AudioTranscriptionService audioTranscriptionService;
    private final StreamingChatService streamingChatService;

    public ChatController(EnhancedChatCallService enhancedChatCallService, 
                         FastAIService fastAIService,
                         LightweightAIService lightweightAIService,
                         AudioTranscriptionService audioTranscriptionService,
                         StreamingChatService streamingChatService) {
        this.enhancedChatCallService = enhancedChatCallService;
        this.fastAIService = fastAIService;
        this.lightweightAIService = lightweightAIService;
        this.audioTranscriptionService = audioTranscriptionService;
        this.streamingChatService = streamingChatService;
        log.info("🚀 ChatController initialized with LIGHTNING AI performance optimization + STREAMING SUPPORT");
    }

    @Operation(summary = "Process chat message with PERFORMANCE OPTIMIZATION")
    @PostMapping(value = "/call", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String handleCall(@RequestBody AIMessageDTO message) {
        log.info("🔎 Received AIMessage: {}; sessionId: {}", message.getMessage(), message.getSessionId());

        if (message.getMessage() == null || message.getMessage().isEmpty()) {
            log.warn("⚠️ No text message received. Possible missing transcription.");
            return "No text message provided.";
        }

        // PERFORMANCE OPTIMIZATION: Use FastAIService for speed-critical requests
        long startTime = System.currentTimeMillis();
        
        try {
            String response;
            
            // LIGHTNING MODE: Use ultra-lightweight service for maximum speed
            boolean isSimpleQuery = isSimpleTravelQuery(message.getMessage());
            if (isSimpleQuery) {
                log.info("⚡ Using LIGHTNING mode for sub-2s response");
                response = lightweightAIService.processUltraFast(message);
            } else if (fastAIService.shouldUseFastMode(message.getMessage())) {
                log.info("🚀 Using FastAI for performance optimization");
                response = fastAIService.processWithTimeout(message);
            } else {
                log.info("🔄 Using standard processing for complex request");
                response = enhancedChatCallService.processCallMessage(message);
            }
            
            long totalTime = System.currentTimeMillis() - startTime;
            log.info("⚡ Response completed in {}ms - Performance: {}", 
                totalTime, 
                totalTime < 2000 ? "🏆 EXCELLENT" : totalTime < 5000 ? "✅ GOOD" : "⚠️ NEEDS IMPROVEMENT");
                
            return response;
            
        } catch (Exception e) {
            long errorTime = System.currentTimeMillis() - startTime;
            log.error("❌ Chat processing error after {}ms: {}", errorTime, e.getMessage());
            return "I apologize, but I'm experiencing technical difficulties. Please try rephrasing your question, and I'll do my best to help you plan your trip!";
        }
    }

    @Operation(summary = "Stream chat message with progressive rendering (SSE)")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> handleStreamingCall(
            @RequestParam String message,
            @RequestParam String sessionId) {
        
        log.info("🌊 Received streaming request - sessionId: {}, message: '{}'", 
                 sessionId, 
                 message.substring(0, Math.min(50, message.length())));

        if (message == null || message.isEmpty()) {
            log.warn("⚠️ No text message received for streaming.");
            return Flux.just("❌ No message provided.");
        }

        try {
            // Create AIMessageDTO for service layer
            AIMessageDTO messageDTO = new AIMessageDTO();
            messageDTO.setMessage(message);
            messageDTO.setSessionId(sessionId);

            // Stream response using reactive service
            return streamingChatService.streamResponse(messageDTO)
                .doOnSubscribe(subscription -> {
                    log.info("🎬 Streaming subscription started for session: {}", sessionId);
                })
                .doOnComplete(() -> {
                    log.info("✅ Streaming response completed for session: {}", sessionId);
                })
                .doOnError(error -> {
                    log.error("❌ Streaming error for session {}: {}", sessionId, error.getMessage(), error);
                });

        } catch (Exception e) {
            log.error("❌ Error initiating stream for session {}: {}", sessionId, e.getMessage(), e);
            return Flux.just("❌ Failed to initiate streaming response. Please try the standard chat endpoint.");
        }
    }

    @Operation(summary = "Process chat message and return suggestions")
    @PostMapping(value = "/call/entity", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AIMessageResponse> handleEntityCall(
            @RequestBody(required = false) AIMessageDTO message,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        log.info("📨 Received entity message with sessionId: {}", message != null ? message.getSessionId() : "No Message");

        AIMessageResponse response = enhancedChatCallService.processEntityMessage(message, AIMessageResponse.class);
        
        // Debug logging to track response
        log.info("🔍 Debug - AI Response object: {}", response);
        log.info("🔍 Debug - Response message: {}", response != null ? response.getMessage() : "null");
        log.info("🔍 Debug - Response suggestions: {}", response != null ? response.getSuggestions() : "null");
        
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Upload audio file for transcription")
    @PostMapping(value = "/upload-audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAudio(
            @RequestParam("media") MultipartFile file,
            @RequestParam("sessionId") String sessionId) {

        if (file == null || file.isEmpty()) {
            log.error("❌ No audio file received!");
            return ResponseEntity.badRequest().body("No file uploaded.");
        }

        try {
            log.info("✅ Received audio file: {} for session: {}",
                    (file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown"),
                    sessionId);

            String transcription = audioTranscriptionService.transcribeAudio(file);
            log.info("✅ Transcription result: {}", transcription);

            return ResponseEntity.ok(transcription);
        } catch (Exception e) {
            log.error("❌ Error transcribing audio: ", e);
            return ResponseEntity.status(500).body("Error processing audio");
        }
    }
    
    /**
     * Determines if a query is simple enough for lightning-fast processing
     */
    private boolean isSimpleTravelQuery(String message) {
        String lower = message.toLowerCase();
        
        // Simple travel queries that don't need function calls
        return lower.matches(".*(restaurant|hotel|flight|recommend|where to|what to|best|top).*") &&
               !lower.contains("complex") &&
               !lower.contains("detailed") &&
               !lower.contains("itinerary") &&
               message.length() < 200; // Keep it simple
    }
}
