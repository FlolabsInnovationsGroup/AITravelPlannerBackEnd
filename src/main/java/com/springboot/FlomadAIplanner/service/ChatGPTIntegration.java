package com.springboot.FlomadAIplanner.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

public class ChatGPTIntegration {
    private static final String BACKEND_URL = "http://localhost:8080/getPlaces";
    private static final String OPENAI_API_KEY = "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";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/engines/davinci/completions";

    public static String getTravelSuggestions(String lat, String lon, int radius, String kinds) throws Exception {
        String url = String.format("%s?lat=%s&lon=%s&radius=%d&kinds=%s", BACKEND_URL, lat, lon, radius, kinds);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static String getChatGPTResponse(String prompt) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> data = new HashMap<>();
        data.put("prompt", prompt);
        data.put("max_tokens", 150);

        String requestBody = objectMapper.writeValueAsString(data);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static void main(String[] args) {
        try {
            String lat = "40.7128";
            String lon = "-74.0060";  // Coordinates for New York City
            String placesResponse = getTravelSuggestions(lat, lon, 1000, "interesting_places");

            String prompt = "I am in New York City. Can you suggest some interesting places to visit?\n\n" + placesResponse;
            String chatgptResponse = getChatGPTResponse(prompt);

            System.out.println("ChatGPT Response: " + chatgptResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
}
