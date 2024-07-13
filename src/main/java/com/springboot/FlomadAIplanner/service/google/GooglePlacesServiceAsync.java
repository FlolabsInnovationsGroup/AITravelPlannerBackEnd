package com.springboot.FlomadAIplanner.service.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import com.springboot.FlomadAIplanner.exceptions.GoogleAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service class for handling Google Places API interactions.
 */
@Slf4j
@Service
public class GooglePlacesServiceAsync {

    private final GooglePlacesClient googlePlacesClient;

    @Value("${google.places.fieldMask}")
    private String placesFieldMask;

    @Value("${google.textSearch.fieldMask}")
    private String textSearchFieldMask;

    public GooglePlacesServiceAsync(GooglePlacesClient googlePlacesClient) {
        this.googlePlacesClient = googlePlacesClient;
    }

    /**
     * Method to search for nearby places.
     *
     * @param request The request object containing search parameters.
     * @return JSON response with search results.
     */

    @SuppressWarnings("unused")
    @Async
    protected CompletableFuture<JsonNode> searchNearbyPlaces(NearbyPlacesRequest request) {
        log.info("Searching Google nearby places with request: {}", request);
        try {
            JsonNode response = googlePlacesClient.searchNearbyPlaces(placesFieldMask, request);
            log.info("Received response from Google Places API: {}", response);
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Error while searching nearby places", e);
            return CompletableFuture.completedFuture(JsonNodeFactory.instance.objectNode().put("error", e.getMessage()));
        }
    }

    @Async
    protected CompletableFuture<JsonNode> asyncTextSearchPlaces(TextSearchRequest request) {
        log.info("Searching Google places with request: {}", request);
        try {
            JsonNode response = googlePlacesClient.textSearchPlaces(textSearchFieldMask, request);
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Error performing text search for query: {}", request.getTextQuery(), e);
            return CompletableFuture.completedFuture(JsonNodeFactory.instance.objectNode().put("error", e.getMessage()));
        }
    }

    /**
     * Method to search for nearby places.
     *
     * @param requests The list of requests containing search parameters.
     * @return JSON response with search results.
     * @throws GoogleAPIException If an error occurs while calling the Google Places API.
     */
    public JsonNode bulkTextSearchPlaces(List<TextSearchRequest> requests) throws GoogleAPIException {
        log.info("Performing bulk text search with {} queries", requests);
        List<CompletableFuture<JsonNode>> futures = requests.stream()
                .map(this::asyncTextSearchPlaces)
                .toList();

        ArrayNode results = JsonNodeFactory.instance.arrayNode();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join(); // Ensure all complete

        futures.forEach(f -> {
            try {
                results.add(f.join());
            } catch (Exception e) {
                log.error("Failed to get future result", e);
                results.add(JsonNodeFactory.instance.objectNode().put("error", "Failed to get result"));
            }
        });

        return results;
    }


}

