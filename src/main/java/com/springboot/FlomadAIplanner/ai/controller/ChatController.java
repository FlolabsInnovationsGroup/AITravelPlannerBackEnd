package com.springboot.FlomadAIplanner.ai.controller;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageResponse;
import com.springboot.FlomadAIplanner.ai.service.ChatCallService;
import com.springboot.FlomadAIplanner.ai.agents.audio.AudioTranscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatCallService chatCallService;
    private final AudioTranscriptionService audioTranscriptionService;

    public ChatController(ChatCallService chatCallService, AudioTranscriptionService audioTranscriptionService) {
        this.chatCallService = chatCallService;
        this.audioTranscriptionService = audioTranscriptionService;
    }

    @Operation(summary = "Process chat message with optional media")
    @PostMapping(value = "/call", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String handleCall(@RequestBody AIMessageDTO message) {
        log.info("🔎 Received AIMessage: {}; sessionId: {}", message.getMessage(), message.getSessionId());

        if (message.getMessage() == null || message.getMessage().isEmpty()) {
            log.warn("⚠️ No text message received. Possible missing transcription.");
            return "No text message provided.";
        }

        return chatCallService.processMessage(message);
    }

    @Operation(summary = "Process chat message and return suggestions")
    @PostMapping(value = "/call/entity", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AIMessageResponse> handleEntityCall(
            @RequestBody(required = false) AIMessageDTO message,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        log.info("📨 Received entity message with sessionId: {}", message != null ? message.getSessionId() : "No Message");

        AIMessageResponse response = chatCallService.processEntityMessage(message, AIMessageResponse.class);
        
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
}
