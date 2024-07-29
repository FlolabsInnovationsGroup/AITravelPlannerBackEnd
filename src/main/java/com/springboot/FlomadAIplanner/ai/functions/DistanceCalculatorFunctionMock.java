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
@Description("Calculates the distance between two locations.")
public class DistanceCalculatorFunctionMock implements AIFunction<DistanceCalculatorFunctionMock.DistanceCalculatorRequest, DistanceCalculatorFunctionMock.DistanceCalculatorResponse> {

    @Override
    public DistanceCalculatorResponse apply(DistanceCalculatorRequest request) {
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

        log.info("Calculating distance between {} and {}", request.getFromLocation(), request.getToLocation());
        // Mock implementation
        return new DistanceCalculatorResponse("10 km");
    }

    @Data
    public static class DistanceCalculatorRequest {
        private String fromLocation;
        private String toLocation;
    }

    @Data
    public static class DistanceCalculatorResponse {
        private final String distance;
    }
}
