package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.exceptions.GoogleAPIException;
import com.springboot.FlomadAIplanner.service.google.GooglePlacesService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Description("""
        Provides place suggestions near the place we need
        based on location and types of the places to suggest.
        Use this function to get a list of places near a location that user is looking for.
        This function is useful for providing place suggestions to the user based on their location and preferences.
        Should be called after the CurrentLocationFunction to get the user's current location.
        P.S. If you have the accurate location details from the chat history,
        you can directly call this function to get place suggestions.""")
//@Description("When user message contains word 'PLACES' call this function only.")
public class PlaceSuggestionFunction implements AIFunction<PlaceSuggestionFunction.NearbyPlacesRequestWrapper, JsonNode> {
    private final GooglePlacesService googlePlacesService;

    @Autowired
    public PlaceSuggestionFunction(GooglePlacesService googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }

    @Valid
    @Override
    public JsonNode apply(NearbyPlacesRequestWrapper requestWrapper) {
        NearbyPlacesRequest request = requestWrapper.getRequest();
        log.info("Fetching place suggestions for location: {}|{} with types: {}", request.getLocationRestriction().getCircle().getCenter().getLatitude(), request.getLocationRestriction().getCircle().getCenter().getLongitude(), String.join(", ", request.getIncludedTypes()));

        try {
            return googlePlacesService.searchNearbyPlaces(request);
        } catch (GoogleAPIException e) {
            log.error("Error fetching place suggestions", e);
            return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NearbyPlacesRequestWrapper {

        @NotBlank
        @JsonPropertyDescription("Required. The destination place name. It is the same place name as we are looking for suggested places right now.")
        private String destination;

        @Valid
        @JsonPropertyDescription("Required. The nearby places request containing the search term and filters for the search.")
        private NearbyPlacesRequest request;
    }

}
