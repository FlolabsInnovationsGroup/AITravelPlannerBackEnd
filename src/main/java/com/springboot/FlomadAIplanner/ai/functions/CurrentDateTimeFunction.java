package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.ai.ChatBot;
import com.springboot.FlomadAIplanner.handler.voice.VoiceHandler;
import jakarta.validation.constraints.NotBlank;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@Description("""
        Retrieves the current date and time.
        Use this function to get the current date and time in a specified format.
        This function is useful for getting context to AI model about the current date and time in the real world.
        MUST be called BEFORE any other function that requires date and time parameter.""")
public class CurrentDateTimeFunction implements AIFunction<CurrentDateTimeFunction.CurrentDateTimeRequest, JsonNode> {

    @Override
    public JsonNode apply(CurrentDateTimeRequest request) {
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

        log.info("Retrieving current date and time");
        try {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(request.getFormat());
            String formattedDateTime = now.format(formatter);
            log.info("Current date and time: {}", formattedDateTime);
            return JsonNodeFactory.instance.objectNode().put("currentDateTime", formattedDateTime);
        } catch (Exception e) {
            log.error("Error retrieving current date and time", e);
            return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentDateTimeRequest {
        @NotBlank
        @JsonPropertyDescription("Required. Specifies the format in which the current date and time should be returned. Example: yyyy-MM-dd HH:mm:ss")
        private String format;
    }
}
