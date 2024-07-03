package com.springboot.FlomadAIplanner.ai.controller;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.service.ChatCallService;
import com.springboot.FlomadAIplanner.ai.service.ChatStreamService;
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

    @PostMapping("/call")
    public String handleCall(@RequestBody AIMessageDTO message) {
        return chatCallService.processMessage(message);
    }

    @PostMapping("/stream")
    public Flux<String> handleStream(@RequestBody AIMessageDTO message) {
        return chatStreamService.processMessage(message);
    }

//    @PostMapping("/planTrip")
//    public TripPlanDTO planTrip(@RequestBody String message) {
//        return chatClientService.planTrip(message);
//    }

}
