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

@Slf4j
@Component
@Description("""
        Provides place information based on a text query.
        Use this function to get detailed information about a place based on a text search.
        This function MUST be used to obtain the exact location (longitude and latitude),
        before calling the PlaceSuggestion Function for exact location,
        we need to obtain (longitude and latitude) of this place.
        and Google Maps URL for a place.""")
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
