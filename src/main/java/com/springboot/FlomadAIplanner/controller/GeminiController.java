package com.springboot.FlomadAIplanner.controller;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.springboot.FlomadAIplanner.DTO.OpenAIDataDTO;
import com.springboot.FlomadAIplanner.service.GeminiService;
import com.springboot.FlomadAIplanner.service.OpenAIService;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

////////////////////////////////////////////
/**
 * @class
 * @description This is a controller that allows you to talk to Gemini API
 */
//////////////////////////////////////////
@RestController
public class GeminiController {
    /////////////////////////
    //todo: implement Gemini Vertex pro here

    private final GeminiService geminiService;
    
    @Autowired
    private OpenAIService openAIService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/generate-content/Gemini")
    public ResponseEntity<String> generateContent(@RequestBody String text) {
        String response = geminiService.callGeminiApi(text);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/generate-content")
    public String getResponseFromOpenAI(@RequestBody OpenAIDataDTO request) {
        try {
            return openAIService.sendMessageToOpenAI(request.getMessage());
        } catch (Exception e) {
            return "Error while calling OpenAI: " + e.getMessage();
        }
    }
}