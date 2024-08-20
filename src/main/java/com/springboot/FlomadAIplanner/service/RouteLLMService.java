package com.springboot.FlomadAIplanner.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

@Slf4j
@Component
public class RouteLLMService {
    private final RestTemplate restTemplate;

    public RouteLLMService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public String getRouteLLMResponse(String input) {
        String url = "http://localhost:6060/v1/chat/completions";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Constructing the request body
    String requestBody = String.format("{\"model\":\"chatgpt-v1-threshold\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}", input);
    
    HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
    try {
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody();
    } catch (Exception e) {
        e.printStackTrace();
        return "Error occurred: " + e.getMessage();
    }
}
}
