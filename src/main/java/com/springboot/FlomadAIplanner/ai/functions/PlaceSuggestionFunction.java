package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.exceptions.api.GoogleAPIException;
import com.springboot.FlomadAIplanner.service.google.GooglePlacesService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
public class PlaceSuggestionFunction implements Function<PlaceSuggestionFunction.NearbyPlacesRequestWrapper, List<JsonNode>> {
    private final GooglePlacesService googlePlacesService;

    @Autowired
    public PlaceSuggestionFunction(GooglePlacesService googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }

    @Valid
//    @Override
    public List<JsonNode> apply(NearbyPlacesRequestWrapper requestWrapper) {
        List<JsonNode> responses = new ArrayList<>();

        for (NearbyPlacesRequest request : requestWrapper.getRequests()) {
            log.info("Fetching place suggestions for location: {}|{} with types: {}",
                    request.getLocationRestriction().getCircle().getCenter().getLatitude(),
                    request.getLocationRestriction().getCircle().getCenter().getLongitude(), String.join(", ", request.getIncludedTypes()));
            try {
                JsonNode response = googlePlacesService.searchNearbyPlaces(request);
                log.info("Received place suggestions from Google. Response length: {}", response.toString().length());
                responses.add(response);
            } catch (GoogleAPIException e) {
                log.error("Error fetching place suggestions", e);
                responses.add(JsonNodeFactory.instance.objectNode().put("error", e.getMessage()));
            }
        }

        return responses;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NearbyPlacesRequestWrapper {

        @Valid
        @JsonPropertyDescription("List of nearby places requests containing the search terms and filters for each search.")
        private List<NearbyPlacesRequest> requests;
    }
}
