package com.springboot.FlomadAIplanner.service.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.exceptions.GoogleAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GooglePlacesService {

    private final GooglePlacesClient googlePlacesClient;

    @Value("${google.places.fieldMask}")
    private String fieldMask;

    public GooglePlacesService(GooglePlacesClient googlePlacesClient) {
        this.googlePlacesClient = googlePlacesClient;
    }

    public JsonNode searchNearbyPlaces(NearbyPlacesRequest request) throws GoogleAPIException {
        log.info("Searching Google nearby places with request: {}", request);
        try {
            JsonNode response = googlePlacesClient.searchNearbyPlaces(fieldMask, request);
            log.info("Received response from Google Places API");
            return response;
        } catch (Exception e) {
            log.error("Error while searching nearby places", e);
            throw new GoogleAPIException("Error while searching nearby places", e);
        }
    }

}
