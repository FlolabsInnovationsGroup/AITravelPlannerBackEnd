package com.springboot.FlomadAIplanner.service.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

/**
 * Service class for handling Google Places API interactions asynchronously.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GooglePlacesServiceAsync {

    private final GooglePlacesClient googlePlacesClient;

    @Value("${google.api.key}") // ✅ Ensure API key is correctly injected
    private String googleApiKey;

    @Value("${google.places.fieldMask}")
    private String placesFieldMask;

    @Value("${google.textSearch.fieldMask}")
    private String textSearchFieldMask;

    /**
     * Asynchronously performs a search for nearby places using the provided request.
     *
     * @param request The request object containing search parameters.
     * @return a CompletableFuture containing the JSON response from the search.
     */
    @Async
    public CompletableFuture<JsonNode> searchNearbyPlaces(NearbyPlacesRequest request) {
        log.info("Searching Google nearby places with request: {}", request);
        return CompletableFuture.supplyAsync(() -> {
            try {
                String location = String.format("%f,%f", 
                    request.getLocationRestriction().getCircle().getCenter().getLatitude(),
                    request.getLocationRestriction().getCircle().getCenter().getLongitude());
                int radius = (int) request.getLocationRestriction().getCircle().getRadius();
                String type = String.join("|", request.getIncludedTypes());
                
                return googlePlacesClient.searchNearbyPlaces(
                        googleApiKey,              // API key as query parameter
                        location,                  // Latitude,longitude
                        radius,                    // Search radius in meters
                        type,                      // Place types
                        placesFieldMask            // Field mask
                );
            } catch (Exception e) {
                log.error("Error while searching nearby places", e);
                return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
            }
        });
    }

    /**
     * Asynchronously performs a text search for places using the provided request.
     *
     * @param request The request object containing the text query and other parameters.
     * @return a CompletableFuture containing the JSON response from the search.
     */
    @Async
    public CompletableFuture<JsonNode> asyncTextSearchPlaces(TextSearchRequest request) {
        log.info("Searching Google places with request: {}", request);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return googlePlacesClient.textSearchPlaces(
                        textSearchFieldMask,       // ✅ Correct
                        request                    // ✅ Correct
                );
            } catch (Exception e) {
                log.error("Error performing text search for query: {}", request.getTextQuery(), e);
                return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
            }
        });
    }
}
