package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import com.springboot.FlomadAIplanner.exceptions.api.GoogleAPIException;
import com.springboot.FlomadAIplanner.service.google.GooglePlacesServiceAsync;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Description("""
        Provides place information based on a list of text queries.
        Use this function to get detailed information about one or multiple - [1,∞) places based on text searches.
        Recommended to use for search for all required places in one call.
        This function processes a batch of place names, making it efficient for bulk queries.
        It is especially useful before calling the "PlaceSuggestionFunction" to obtain the exact locations (longitude and latitude) and
        Google Maps URLs for the searched places.
        
        Do not change or edit in any way the URLs of the Google Maps links provided by this function.
        """)
public class PlaceTextSearchFunction implements AIFunction<PlaceTextSearchFunction.TextSearchRequestsWrapper, JsonNode> {

    private final GooglePlacesServiceAsync googlePlacesService;

    @Autowired
    public PlaceTextSearchFunction(GooglePlacesServiceAsync googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }

    @Valid
    @Override
    public JsonNode apply(TextSearchRequestsWrapper requestsWrapper) {
        log.info("Performing text search with query: {}", requestsWrapper.getRequests());
        try {
            JsonNode response = googlePlacesService.bulkTextSearchPlaces(requestsWrapper.getRequests());
            log.info("Received text search response from Google. Response length: {}", response.toString().length());
            return response;
        } catch (GoogleAPIException e) {
            log.error("Error performing text search", e);
            return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextSearchRequestsWrapper {

        @JsonPropertyDescription("List of text search requests. Recommended to use for search for all required places in one call.")
        private List<TextSearchRequest> requests;
    }
}
