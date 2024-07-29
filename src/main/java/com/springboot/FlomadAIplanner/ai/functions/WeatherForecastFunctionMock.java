package com.springboot.FlomadAIplanner.ai.functions;

import com.springboot.FlomadAIplanner.ai.ChatBot;
import com.springboot.FlomadAIplanner.handler.voice.VoiceHandler;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Description("Provides weather forecast for a given location and date.")
public class WeatherForecastFunctionMock implements AIFunction<WeatherForecastFunctionMock.WeatherForecastRequest, WeatherForecastFunctionMock.WeatherForecastResponse> {

    @Override
    public WeatherForecastResponse apply(WeatherForecastRequest request) {
        ChatBot chatBot;
        chatBot = new ChatBot();
        TextField userInputField;
        userInputField = new TextField();
        TextArea chatArea;
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(200);
        String userInput = userInputField.getText();
        if (userInput.isEmpty()) {
            return null;
        }
        chatArea.appendText("You: " + userInput + "\n");
        userInputField.clear();
        VoiceHandler voiceHandler = new VoiceHandler();
        new Thread(() -> {
            try {

                String response = chatBot.getResponse(userInput);
                chatArea.appendText("AI: " + response + "\n");
                voiceHandler.speakText(response);
            } catch (IOException e) {
                e.printStackTrace();
                chatArea.appendText("AI: Error fetching response\n");
            }
        }).start();

        log.info("Fetching weather forecast for location: {} on date: {}", request.getLocation(), request.getDate());
        // Mock implementation
        return new WeatherForecastResponse("Sunny", 25);
    }

    @Data
    public static class WeatherForecastRequest {
        private String location;
        private String date;
    }

    @Data
    public static class WeatherForecastResponse {
        private final String weather;
        private final int temperature;
    }
}
