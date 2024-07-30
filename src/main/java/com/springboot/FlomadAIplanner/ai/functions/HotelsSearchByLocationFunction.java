package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.ai.ChatBot;
import com.springboot.FlomadAIplanner.dto.amadeus.HotelSearchByGeocodeRequest;
import com.springboot.FlomadAIplanner.dto.amadeus.HotelSearchByGeocodeResponse;
import com.springboot.FlomadAIplanner.exceptions.api.AmadeusAPIException;
import com.springboot.FlomadAIplanner.handler.voice.VoiceHandler;
import com.springboot.FlomadAIplanner.service.amadeus.AmadeusHotelsSearchService;
import jakarta.validation.Valid;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Description("""
        Use this function to perform geocode-based searches for hotels.
        This function accepts longitude and latitude details and other optional parameters to find hotels.
        Before calling this function, use the "PlaceTextSearchFunction" to get the exact location (longitude and latitude) of a required place.
        Geocode search allows specifying exact geographical points using latitude and longitude, and filters based on proximity, amenities, ratings, and hotel sources.
        This method provides a way to discover hotels that are precisely tailored to geographical and preference-based needs.
        To get exact location (GoogleMaps link) for each of the found hotels YOU MUST use the PlaceTextSearchFunction for EVERY found hotel separately.""")
public class HotelsSearchByLocationFunction implements AIFunction<HotelsSearchByLocationFunction.HotelSearchRequestWrapper, HotelSearchByGeocodeResponse> {

    private final AmadeusHotelsSearchService amadeusHotelsSearchService;

    public HotelsSearchByLocationFunction(AmadeusHotelsSearchService amadeusHotelsSearchService) {
        this.amadeusHotelsSearchService = amadeusHotelsSearchService;
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

    @Valid
    public HotelSearchByGeocodeResponse apply(HotelSearchRequestWrapper requestWrapper) {
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
        log.info("Searching hotels by geocode at latitude: {}, longitude: {}", requestWrapper.getRequest().getLatitude(), requestWrapper.getRequest().getLongitude());
        try {
            HotelSearchByGeocodeResponse response = amadeusHotelsSearchService.searchHotelsByGeocode(requestWrapper.getRequest());
            log.info("Received hotel search response from Amadeus. size: {};  Response length: {}", response.getData().size(), response.toString().length());
            return response;
        } catch (AmadeusAPIException e) {
            log.error("Error searching for hotels by geocode", e);
            return HotelSearchByGeocodeResponse.builder()
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotelSearchRequestWrapper {

        @Valid
        @JsonPropertyDescription("The geocode-based hotel search request body containing latitude, longitude, and optional filters.")
        private HotelSearchByGeocodeRequest request;
    }
}
