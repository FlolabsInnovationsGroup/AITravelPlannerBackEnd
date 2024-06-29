package com.springboot.FlomadAIplanner.controller;

import com.springboot.FlomadAIplanner.DTO.OpenAIDataDTO;
import com.springboot.FlomadAIplanner.service.OpenAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class OpenAIController {

    @Autowired
    private OpenAIService openAIService;

    @PostMapping
    public String getResponseFromOpenAI(@RequestBody OpenAIDataDTO request) throws IOException {
        log.info("Generating content for text: {}", request.getMessage());

        return openAIService.sendMessageToOpenAI(request.getMessage());
    }
}