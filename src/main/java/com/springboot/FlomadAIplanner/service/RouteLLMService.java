package com.springboot.FlomadAIplanner.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

@Slf4j
@Service
public class RouteLLMService {

    private final RestTemplate restTemplate;

    public RouteLLMService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getLLMResponse(String message) {
        String url = "http://localhost:6060/v1/chat/completions";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer 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");

        String requestJson = "{ \"model\": \"router-mf-0.11593\", \"messages\": [{ \"role\": \"user\", \"content\": \"" + message + "\" }] }";

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        
        return response.getBody();
    }
}
