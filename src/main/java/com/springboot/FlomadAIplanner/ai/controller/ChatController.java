package com.springboot.FlomadAIplanner.ai.controller;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.service.ChatCallService;
import com.springboot.FlomadAIplanner.ai.service.ChatStreamService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatCallService chatCallService;
    private final ChatStreamService chatStreamService;

    public ChatController(ChatCallService chatCallService, ChatStreamService chatStreamService) {
        this.chatCallService = chatCallService;
        this.chatStreamService = chatStreamService;
    }

    @Operation(summary = "Single message response from LLM",
            description = "Processes a chat message and returns a response in a single message.")
    @PostMapping("/call")
    public ResponseEntity<String> handleCall(@RequestBody AIMessageDTO message, 
                                             @RequestHeader(value = "Session-ID", required = false) String sessionId) {
        System.out.println("Received message: " + message.getMessage()); 
        sessionId = getOrCreateSessionId(sessionId);
        String response = chatCallService.processMessage(message, sessionId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Streamed message response from LLM (Live response generation)",
            description = "Processes a chat message and returns a response in a stream of messages.")
    @PostMapping("/stream")
    public Flux<String> handleStream(@RequestBody AIMessageDTO message, 
                                     @RequestHeader(value = "Session-ID", required = false)String sessionId) {
        sessionId = getOrCreateSessionId(sessionId);
        return chatStreamService.processMessage(message, sessionId);
    }

    private String getOrCreateSessionId(String sessionId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && 
        !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"))) {
        // Return some unique identifier for authenticated users, e.g., their username
        return authentication.getName();
    }        
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }
        return sessionId;
    }
}