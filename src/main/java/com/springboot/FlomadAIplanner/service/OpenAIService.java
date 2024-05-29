package com.springboot.FlomadAIplanner.service;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.FlomadAIplanner.DTO.MessageDTO;
import com.springboot.FlomadAIplanner.controller.DialogflowController;

// import net.minidev.json.JSONArray;
// import net.minidev.json.JSONObject;

import java.io.IOException;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;



@Service
public class OpenAIService {
    private static final Logger logger = Logger.getLogger(OpenAIService.class.getName());

    @Value("${openai.apikey}")
    private String apiKey;

    public String sendMessageToOpenAI(String message) throws IOException {
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

        CloseableHttpResponse response = client.execute(httpPost);
        String result = EntityUtils.toString(response.getEntity(), "UTF-8");
        client.close();

        return result;
    }
}
