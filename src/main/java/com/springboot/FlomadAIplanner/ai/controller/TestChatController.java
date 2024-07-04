package com.springboot.FlomadAIplanner.ai.controller;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.ai.service.AgentService;
import com.springboot.FlomadAIplanner.ai.service.ChatCallService;
import com.springboot.FlomadAIplanner.ai.service.ChatStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/test/chat")
public class TestChatController {
    private final AgentService agentService;

    public TestChatController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/current")
    public Flux<String> handleCurrentChat(@RequestBody AIMessageDTO messages) {
        return agentService.processCurrentChat(messages);
    }

    @PostMapping("/history")
    public Flux<String> handleHistoryChat(@RequestBody AIMessageDTO messages) {
        return agentService.processHistoryChat(messages);
    }

    @PostMapping("/intent")
    public String handleIntentChat(@RequestBody AIMessageDTO messages) {
        return agentService.processIntentExtractor(messages);
    }

}
