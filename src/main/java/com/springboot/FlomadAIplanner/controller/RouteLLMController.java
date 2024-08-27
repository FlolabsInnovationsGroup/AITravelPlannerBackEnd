package com.springboot.FlomadAIplanner.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.FlomadAIplanner.service.RouteLLMService;

@RestController
public class RouteLLMController {

    private final RouteLLMService routeLLMService;

    public RouteLLMController(RouteLLMService routeLLMService) {
        this.routeLLMService = routeLLMService;
    }

    @GetMapping("/get-llm-response")
    public String getLLMResponse(@RequestParam String message) {
        return routeLLMService.getLLMResponse(message);
    }
}

