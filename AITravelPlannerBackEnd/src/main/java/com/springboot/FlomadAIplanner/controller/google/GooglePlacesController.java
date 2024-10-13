package com.springboot.FlomadAIplanner.controller.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.exceptions.api.GoogleAPIException;
import com.springboot.FlomadAIplanner.service.google.GooglePlacesService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling Google Places API requests.
 */
@Slf4j
@RestController
@RequestMapping("/api/places")
public class GooglePlacesController {

    private final GooglePlacesService googlePlacesService;

    public GooglePlacesController(GooglePlacesService googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }

    /**
     * Endpoint to search for nearby places.
     *
     * @param nearbyPlacesRequest The request object containing search parameters.
     * @return JSON response with search results.
     * @throws GoogleAPIException If an error occurs while calling the Google Places API.
     */
    @Operation(summary = "Search for nearby places", description = "Searches for nearby places using Google Places API")
    @PostMapping("/search")
    public JsonNode searchNearbyPlaces(@RequestBody NearbyPlacesRequest nearbyPlacesRequest) throws GoogleAPIException {
        log.info("Searching for nearby places using Google API with request:{}", nearbyPlacesRequest);
        return googlePlacesService.searchNearbyPlaces(nearbyPlacesRequest);
    }
}


