package com.springboot.FlomadAIplanner.controller;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.springboot.FlomadAIplanner.DTO.MessageDTO;
import com.springboot.FlomadAIplanner.DTO.OpenAIDataDTO;
import com.springboot.FlomadAIplanner.service.GeminiService;
import com.springboot.FlomadAIplanner.service.LLMService;
import com.springboot.FlomadAIplanner.service.OpenAIService;

import java.io.IOException;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
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
public class LLMController {
    /////////////////////////
    //todo: implement Gemini Vertex pro here
    
    private static final Logger logger = LoggerFactory.getLogger(LLMController.class);

    private final GeminiService geminiService;

    @Autowired
    private LLMService llmService;
    
    @Autowired
    private OpenAIService openAIService;

    public LLMController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/generate-content/Gemini")
    public ResponseEntity<String> generateContent(@RequestBody String text) {
        String response = geminiService.sendMessageToGemini(text);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/generate-content/OpenAI")
    public String getResponseFromOpenAI(@RequestBody OpenAIDataDTO request) {
        try {
            return openAIService.sendMessageToOpenAI(request.getMessage());
        } catch (Exception e) {
            return "Error while calling OpenAI: " + e.getMessage();
        }
    }

    
    @PostMapping("/generate-content/LLM")
    public ResponseEntity<String> getResponseFromLLM(@RequestBody List<MessageDTO> messages) throws Exception {
        // JSONObject intentJson = llmService.intentRecognition(messages);
        String intentResponse = llmService.intentRecognition(messages);
        JSONObject intentJson = new JSONObject(intentResponse);
        String content = intentJson
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
        int startIndex = content.indexOf("{");
        int endIndex = content.lastIndexOf("}");
        if (startIndex >= 0 && endIndex >= 0) {
            content = content.substring(startIndex, endIndex + 1);
        }
        content = content.replaceAll("\\n", "");
        content = content.replaceAll("\\t", "");
        
        JSONObject contentJson = new JSONObject(content);
        String intent = contentJson.getString("intent");
        String response;
        if ("plan".equals(intent)) {
            response = llmService.inference(messages,"PlanningPrompt");
        } else if ("normal".equals(intent)) {
            response = llmService.inference(messages,"NormalPrompt");
        } else {
            response = contentJson.getString("response");
        }

        JSONObject responseJson = new JSONObject(response);
        responseJson.put("type", intent);
        return ResponseEntity.ok(responseJson.toString());
        // return ResponseEntity.ok(response);

    }

    // @PostMapping("/generate-content/LLM")
    // public String getResponseFromLLM(@RequestBody OpenAIDataDTO request) {
    //     try {
    //         return llmService.inference(request.getMessage());
    //     } catch (Exception e) {
    //         return "Error while calling OpenAI: " + e.getMessage();
    //     }
    // }
}