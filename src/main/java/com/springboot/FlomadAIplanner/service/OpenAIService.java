package com.springboot.FlomadAIplanner.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Slf4j
@Service
public class OpenAIService {
    @Value("${openai.apikey}")
    private String apiKey;

    public String sendMessageToOpenAI(String message) throws IOException {
        log.info("Sending message to OpenAI: {}", message);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://api.openai.com/v1/chat/completions");

        httpPost.setHeader("Authorization", "Bearer " + apiKey);
        httpPost.setHeader("Content-Type", "application/json");

        JSONObject requestBody = new JSONObject();
        // requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("model", "gpt-4o");

        JSONArray messagesArray = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messagesArray.put(userMessage);

        requestBody.put("messages", messagesArray);
        StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
        httpPost.setEntity(entity);

        log.info("Sending request to OpenAI");
        CloseableHttpResponse response = client.execute(httpPost);
        String result = EntityUtils.toString(response.getEntity(), "UTF-8");
        client.close();
        log.info("Received response from OpenAI");

        return result;
    }
}
