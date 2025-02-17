package com.springboot.FlomadAIplanner.ai.controller;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageResponse;
import com.springboot.FlomadAIplanner.ai.service.ChatCallService;
import com.springboot.FlomadAIplanner.ai.service.ChatStreamService;
import com.springboot.FlomadAIplanner.ai.agents.audio.AudioTranscriptionService;  // ✅ Import transcription service
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;  // ✅ Import logging
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@Slf4j  // ✅ Enable logging
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:3000")  // ✅ Fix CORS issue
public class ChatController {

    private final ChatCallService chatCallService;
    private final ChatStreamService chatStreamService;
    private final AudioTranscriptionService audioTranscriptionService;  // ✅ Add this field

    // ✅ Constructor injection
    @Autowired
    public ChatController(ChatCallService chatCallService, ChatStreamService chatStreamService,
                          AudioTranscriptionService audioTranscriptionService) {
        this.chatCallService = chatCallService;
        this.chatStreamService = chatStreamService;
        this.audioTranscriptionService = audioTranscriptionService;  // ✅ Inject service
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


    @Operation(summary = "Process chat message and return suggestions",
            description = "Handles a chat message and returns the AI response along with suggested next inputs.")
    @PostMapping(value = "/call/entity", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AIMessageResponse> handleEntityCall(@ModelAttribute AIMessageDTO message) {
        AIMessageResponse response = chatCallService.processEntityMessage(message, AIMessageResponse.class);
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
            log.info("✅ Received audio file: {} for session: {}", file.getOriginalFilename(), sessionId);

            String transcription = audioTranscriptionService.transcribeAudio(file);
            log.info("✅ Transcription result: {}", transcription);

            return ResponseEntity.ok(transcription);
        } catch (Exception e) {
            log.error("❌ Error transcribing audio: ", e);
            return ResponseEntity.status(500).body("Error processing audio");
        }
    }

}
