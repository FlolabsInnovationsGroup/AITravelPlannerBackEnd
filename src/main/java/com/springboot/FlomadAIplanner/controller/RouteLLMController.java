package com.springboot.FlomadAIplanner.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.springboot.FlomadAIplanner.dto.ChatRequest;
import com.springboot.FlomadAIplanner.service.RouteLLMService;

@RestController
public class RouteLLMController {

    @Autowired
    private RouteLLMService routeLLMService;

    @PostMapping("/api/chat/completions")
    public String createChatCompletion(@RequestBody ChatRequest chatRequest) {
        return routeLLMService.createChatCompletion(chatRequest.getModel(), chatRequest.getMessages());
    }
}


