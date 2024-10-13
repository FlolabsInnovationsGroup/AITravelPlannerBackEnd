package com.springboot.FlomadAIplanner.ai.controller;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.service.ChatCallService;
import com.springboot.FlomadAIplanner.ai.service.ChatStreamService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public String handleCall(@RequestBody AIMessageDTO message) {
        return chatCallService.processMessage(message);
    }

    @Operation(summary = "Streamed message response from LLM (Live response generation)",
            description = "Processes a chat message and returns a response in a stream of messages.")
    @PostMapping("/stream")
    public Flux<String> handleStream(@RequestBody AIMessageDTO message) {
        return chatStreamService.processMessage(message);
    }

}