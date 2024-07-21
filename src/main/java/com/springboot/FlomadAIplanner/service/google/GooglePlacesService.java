package com.springboot.FlomadAIplanner.service.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import com.springboot.FlomadAIplanner.exceptions.api.GoogleAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for handling Google Places API interactions.
 */
@Slf4j
@Service
public class GooglePlacesService {

    private final GooglePlacesClient googlePlacesClient;

    @Value("${google.places.fieldMask}")
    private String placesFieldMask;

    @Value("${google.textSearch.fieldMask}")
    private String textSearchFieldMask;

    public GooglePlacesService(GooglePlacesClient googlePlacesClient) {
        this.googlePlacesClient = googlePlacesClient;
    }

    /**
     * Method to search for nearby places.
     *
     * @param request The request object containing search parameters.
     * @return JSON response with search results.
     * @throws GoogleAPIException If an error occurs while calling the Google Places API.
     */

    public JsonNode searchNearbyPlaces(NearbyPlacesRequest request) throws GoogleAPIException {
        log.info("Searching Google nearby places with request: {}", request);
        try {
            JsonNode response = googlePlacesClient.searchNearbyPlaces(placesFieldMask, request);
            log.info("Received response from Google Places API: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error while searching nearby places", e);
            throw new GoogleAPIException("Error while searching nearby places", e);
        }
    }

    @SuppressWarnings("unused")
    public JsonNode textSearchPlaces(TextSearchRequest request) throws GoogleAPIException {
        log.info("Searching Google places with request: {}", request);
        try {
            JsonNode response = googlePlacesClient.textSearchPlaces(textSearchFieldMask, request);
            log.info("Received response from Google Places API");
            return response;
        } catch (Exception e) {
            log.error("Error while searching places", e);
            throw new GoogleAPIException("Error while searching places", e);
        }
    }

    @SuppressWarnings("unused")
    public JsonNode bulkTextSearchPlaces(List<TextSearchRequest> requests) {
        ArrayNode results = JsonNodeFactory.instance.arrayNode();
        log.info("Performing bulk text searches for multiple queries.");
        for (TextSearchRequest request : requests) {
            try {
                JsonNode searchResult = googlePlacesClient.textSearchPlaces(textSearchFieldMask, request);
                results.add(searchResult);
            } catch (Exception e) {
                log.error("Error performing text search for query: {}", request.getTextQuery(), e);
                results.add(JsonNodeFactory.instance.objectNode().put("error", e.getMessage()));
            }
        }
        return results;
    }

}
