package com.springboot.FlomadAIplanner.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/predict")
    public float[] predict(@RequestBody float[] inputData) {
        return aiService.predict(inputData);
    }
}

