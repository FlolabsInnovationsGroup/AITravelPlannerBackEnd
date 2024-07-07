package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import com.springboot.FlomadAIplanner.exceptions.GoogleAPIException;
import com.springboot.FlomadAIplanner.service.google.GooglePlacesService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * Provides place information based on a text query using the Google Places API.
 * <p>
 * This function helps retrieve detailed information about a place by performing a text search
 * on the Google Places API. It's designed to be used within the Flomad AI planner application.
 * <p>
 * **Important Note:** This function is crucial for obtaining the exact longitude and latitude of a
 * place before using the `PlaceSuggestionFunction`. The exact location coordinates are necessary for
 * accurate place suggestions. Additionally, this function retrieves the Google Maps URL for the
 * searched place.
 */

@Slf4j
@Component
@Description("""
        Provides place information based on a text query.
        Use this function to get detailed information about a place based on a text search.
        Before calling the "PlaceSuggestionFunction', use "PlaceTextSearchFunction" to
        obtain the exact location (longitude and latitude) and obtain
        the Google Maps URL for a place.""")
public class PlaceTextSearchFunction implements AIFunction<TextSearchRequest, JsonNode> {

    private final GooglePlacesService googlePlacesService;

    @Autowired
    public PlaceTextSearchFunction(GooglePlacesService googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }

    @Valid
    @Override
    public JsonNode apply(TextSearchRequest request) {
        log.info("Performing text search with query: {}", request.getTextQuery());
        try {
            return googlePlacesService.textSearchPlaces(request);
        } catch (GoogleAPIException e) {
            log.error("Error performing text search", e);
            return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
        }
    }
}
