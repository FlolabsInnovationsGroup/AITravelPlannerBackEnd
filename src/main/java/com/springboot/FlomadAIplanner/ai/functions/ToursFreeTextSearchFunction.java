package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.ai.ChatBot;
import com.springboot.FlomadAIplanner.dto.viator.BulkFreeTextSearchRequest;
import com.springboot.FlomadAIplanner.dto.viator.FreeTextSearchResponse;
import com.springboot.FlomadAIplanner.exceptions.api.ViatorAPIException;
import com.springboot.FlomadAIplanner.handler.voice.VoiceHandler;
import com.springboot.FlomadAIplanner.service.viator.ViatorAuxiliaryServiceAsync;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@Description("""
        Use this function to perform a free text search for TOURS to the required destinations (countries, cities, famous places, etc.).
        Searching for Viator PRODUCTS, ATTRACTIONS, and DESTINATIONS.
        Use this function every time if you need to search for various activities, tours, and experiences available for each different place required, in case needed to do so.
        Recommended to use for search for tours in all required places in one call. SearchTerms - [1,∞)
        Mostly could be called to find out general information about a tours (payed activities)\s
        or famous places available in the desired destination.
        'request' - input param is REQUIRED!!!
        DESTINATION could be a country, city, town, etc.
        ATTRACTION could be a famous place, monument, museum, etc.
        PRODUCT could be a tour, activity, experience, etc.
        'productUrl' - is actually the link to the tour purchase and description, media.
        You must use CurrentDateTimeFunction before this function to get the current date and time in the real world.""")
public class ToursFreeTextSearchFunction implements AIFunction<ToursFreeTextSearchFunction.FreeTextSearchRequestWrapper, List<FreeTextSearchResponse>> {

    private final ViatorAuxiliaryServiceAsync viatorAuxiliaryService;

    @Autowired
    public ToursFreeTextSearchFunction(ViatorAuxiliaryServiceAsync viatorAuxiliaryService) {
        this.viatorAuxiliaryService = viatorAuxiliaryService;
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
    @Valid
    public List<FreeTextSearchResponse> apply(FreeTextSearchRequestWrapper requestWrapper) {
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

        log.info("Performing free text search with term: {}", requestWrapper.getRequest().getSearchTerms());
        try {
            List<FreeTextSearchResponse> response = viatorAuxiliaryService.bulkSearchFreeText(requestWrapper.getAcceptLanguage(), requestWrapper.getRequest());
            log.info("Received free text search response from Viator. Response length: {}", response.toString().length());
            return response;
        } catch (ViatorAPIException e) {
            log.error("Error performing free text search", e);
            return Collections.singletonList(FreeTextSearchResponse.builder().errorMessage(e.getMessage()).build());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FreeTextSearchRequestWrapper {

        @NotBlank
        @JsonPropertyDescription("REQUIRED. Can't be null. Specifies the language into which the natural-language fields in the response will be translated if needed. Example: en-US. By default use en-US")
        private String acceptLanguage = "en-US";

        @Valid
        @NotNull
        @JsonPropertyDescription("REQUIRED. Can't be null. The free text search request containing the search term and filters for the search.")
        private BulkFreeTextSearchRequest request;
    }

}
