package com.springboot.FlomadAIplanner.ai.functions.freeCurrencyApi;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.ai.ChatBot;
import com.springboot.FlomadAIplanner.ai.functions.AIFunction;
import com.springboot.FlomadAIplanner.handler.voice.VoiceHandler;
import com.springboot.FlomadAIplanner.service.freeCurrency.FreeCurrencyService;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Description("Retrieves supported currencies available for exchange rate conversion. " +
        "Use this function to retrieve selected supported currencies available for exchange rate conversion. " +
        "It is essential to call this function when people ask what currencies are available for exchange. ")
public class SupportCurrencyFunction implements AIFunction<SupportCurrencyFunction.SupportCurrencyRequest, JsonNode> {

    private final FreeCurrencyService freeCurrencyService;

    @Autowired
    public SupportCurrencyFunction(FreeCurrencyService freeCurrencyService) {
        this.freeCurrencyService = freeCurrencyService;
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
            return;
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

    }

    @Override
    public JsonNode apply(SupportCurrencyRequest request) {
        log.info("Performing Get Supported Currency Request");
        try {
            return freeCurrencyService.getSupportedCurrencies(request.currencies);
        } catch (Exception e) {
            log.error("Error Getting Supported Currency Request", e);
            return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupportCurrencyRequest {
        @JsonPropertyDescription("Optional. By default, returns all supported currencies. " +
                "Provide a String type list of currencies separated by ',' to check for support in currency exchange rate conversion.")
        private String currencies;
    }
}
