package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.exceptions.GoogleAPIException;
import com.springboot.FlomadAIplanner.service.google.GooglePlacesService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Description("Provides place suggestions based on location and types. " +
        "Use this function to get a list of places near a location that match the specified types. " +
        "This function is useful for providing place suggestions to the user based on their location and preferences. " +
        "Should be called after the CurrentLocationFunction to get the user's current location.")
public class PlaceSuggestionFunction implements AIFunction<NearbyPlacesRequest, JsonNode> {

    private final GooglePlacesService googlePlacesService;

    @Autowired
    public PlaceSuggestionFunction(GooglePlacesService googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }

    @Valid
    @Override
    public JsonNode apply(NearbyPlacesRequest request) {
        log.info("Fetching place suggestions for location: {}|{} on types: {}",
                request.getLocationRestriction().getCircle().getCenter().getLatitude(),
                request.getLocationRestriction().getCircle().getCenter().getLongitude(),
                String.join(", ", request.getIncludedTypes()));
        try {
            return googlePlacesService.searchNearbyPlaces(request);
        } catch (GoogleAPIException e) {
            log.error("Error fetching place suggestions", e);
            return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
        }
    }

}
