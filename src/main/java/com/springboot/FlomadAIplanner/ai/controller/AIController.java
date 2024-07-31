package com.springboot.FlomadAIplanner.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AIController {
    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @Operation(summary = "Not yet implemented")
    @PostMapping("/predict")
    public float[] predict(@RequestBody float[] inputData) {
        return aiService.predict(inputData);
    }
}

