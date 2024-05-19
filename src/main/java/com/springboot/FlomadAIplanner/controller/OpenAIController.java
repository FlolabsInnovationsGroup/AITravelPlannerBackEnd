package com.springboot.FlomadAIplanner.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.FlomadAIplanner.DTO.OpenAIDataDTO;
import com.springboot.FlomadAIplanner.service.OpenAIService;

@RestController
@RequestMapping("/api/chat")
public class OpenAIController {

    @Autowired
    private OpenAIService openAIService;

    @PostMapping
    public String getResponseFromOpenAI(@RequestBody OpenAIDataDTO request) {
        try {
            return openAIService.sendMessageToOpenAI(request.getMessage());
        } catch (Exception e) {
            return "Error while calling OpenAI: " + e.getMessage();
        }
    }
}