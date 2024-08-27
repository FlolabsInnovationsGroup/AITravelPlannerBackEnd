package com.springboot.FlomadAIplanner.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

@Service
public class RouteLLMService {

    private final String baseUrl = "http://localhost:6060";  // Base URL for RouteLLM

    @Autowired
    private RestTemplate restTemplate;

    public String createChatCompletion(String model, String messages) {
        String url = baseUrl + "/v1/chat/completions";

        // Create the request body as a JSON object
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("frequency_penalty", 0);
        requestBody.put("logit_bias", new JSONObject());
        requestBody.put("logprobs", true);
        requestBody.put("max_tokens", 100);  // You can adjust this as needed
        requestBody.put("n", 1);
        requestBody.put("presence_penalty", 0);
        requestBody.put("response_format", new JSONObject());
        requestBody.put("seed", 0);
        requestBody.put("stop", "");
        requestBody.put("stream", false);
        requestBody.put("temperature", 1);
        requestBody.put("top_p", 1);
        requestBody.put("tools", new JSONObject());
        requestBody.put("tool_choice", "");
        requestBody.put("user", "your-username");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        // Make the HTTP POST request
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // Handle the response
        return response.getBody();
    }
}

