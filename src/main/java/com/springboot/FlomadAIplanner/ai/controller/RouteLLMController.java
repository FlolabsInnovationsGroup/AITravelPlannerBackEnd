package com.springboot.FlomadAIplanner.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.springboot.FlomadAIplanner.service.RouteLLMService;

@RequestMapping("/api")
@RestController
public class RouteLLMController {
    
    @Autowired
    private RouteLLMService routeLLMService;

    @PostMapping("/get-route")
    public String getRoute(@RequestBody String input) {
        return routeLLMService.getRouteLLMResponse(input);
    }
}
