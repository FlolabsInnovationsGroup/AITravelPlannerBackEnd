package com.springboot.FlomadAIplanner.controller;

import com.springboot.FlomadAIplanner.DTO.LLMMessageDTO;
import com.springboot.FlomadAIplanner.DTO.OpenAIDataDTO;
import com.springboot.FlomadAIplanner.service.GeminiService;
import com.springboot.FlomadAIplanner.service.LLMService;
import com.springboot.FlomadAIplanner.service.OpenAIService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @class
 * @description This is a controller that allows you to talk to Gemini API
 */
@Slf4j
@RestController
public class LLMController {
    /////////////////////////
    //todo: implement Gemini Vertex pro here

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
        log.info("Generating content for text: {}", text);
        String response = geminiService.sendMessageToGemini(text);
        log.info("Content generated");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-content/OpenAI")
    public String getResponseFromOpenAI(@RequestBody OpenAIDataDTO request) {
        log.info("Generating content for text: {}", request.getMessage());
        try {
            return openAIService.sendMessageToOpenAI(request.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while calling OpenAI: {}", e.getMessage());
            return "Error while calling OpenAI: " + e.getMessage();
        }
    }


    @PostMapping("/generate-content/LLM")
    public ResponseEntity<String> getResponseFromLLM(@RequestBody List<LLMMessageDTO> messages) throws Exception {
        log.info("Generating content for text: {}", messages);
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
        log.info("Intent recognized");

        String response;
        if ("plan".equals(intent)) {
            response = llmService.inference(messages, "PlanningPrompt");
        } else if ("normal".equals(intent)) {
            response = llmService.inference(messages, "NormalPrompt");
        } else {
            response = contentJson.getString("response");
        }

        JSONObject responseJson = new JSONObject(response);
        responseJson.put("type", intent);

        log.info("Content fully generated");
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