package com.springboot.FlomadAIplanner.service.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import com.springboot.FlomadAIplanner.exceptions.api.GoogleAPIException;
import io.github.bucket4j.Bucket;
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
    private final Bucket rateLimiter;

    @Value("${google.places.fieldMask}")
    private String placesFieldMask;

    @Value("${google.textSearch.fieldMask}")
    private String textSearchFieldMask;

    public GooglePlacesService(GooglePlacesClient googlePlacesClient, Bucket googlePlacesRateLimiter) {
        this.googlePlacesClient = googlePlacesClient;
        this.rateLimiter = googlePlacesRateLimiter;
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
            if (!rateLimiter.tryConsume(1)) {
                throw new GoogleAPIException("Rate limit exceeded. Please try again later.");
            }

            String location = String.format("%f,%f", 
                request.getLocationRestriction().getCircle().getCenter().getLatitude(),
                request.getLocationRestriction().getCircle().getCenter().getLongitude());
            int radius = (int) request.getLocationRestriction().getCircle().getRadius();
            String type = String.join("|", request.getIncludedTypes());
            
            JsonNode response = googlePlacesClient.searchNearbyPlaces(
                    "",                     // Empty string since interceptor handles auth
                    location,               // Latitude,longitude
                    radius,                 // Search radius in meters
                    type,                   // Place types
                    placesFieldMask         // Field mask
            );
            log.info("Received response from Google Places API: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error while searching nearby places", e);
            throw new GoogleAPIException("Error while searching nearby places", e);
        }
    }

    /**
     * Method to perform a text-based search for places.
     *
     * @param request The request object containing search parameters.
     * @return JSON response with search results.
     * @throws GoogleAPIException If an error occurs while calling the Google Places API.
     */
    public JsonNode textSearchPlaces(TextSearchRequest request) throws GoogleAPIException {
        log.info("Searching Google places with request: {}", request);
        try {
            if (!rateLimiter.tryConsume(1)) {
                throw new GoogleAPIException("Rate limit exceeded. Please try again later.");
            }
            JsonNode response = googlePlacesClient.textSearchPlaces(
                    "",                     // Empty string since interceptor handles auth
                    textSearchFieldMask,
                    request
            );
            log.info("Received response from Google Places API: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error while searching places", e);
            throw new GoogleAPIException("Error while searching places", e);
        }
    }

    /**
     * Method to perform a bulk text search for multiple queries.
     *
     * @param requests List of search requests.
     * @return JSON response with search results.
     */
    public JsonNode bulkTextSearchPlaces(List<TextSearchRequest> requests) {
        ArrayNode results = JsonNodeFactory.instance.arrayNode();
        log.info("Performing bulk text searches for multiple queries.");

        for (TextSearchRequest request : requests) {
            try {
                JsonNode searchResult = googlePlacesClient.textSearchPlaces(
                        "",                 // Empty string since interceptor handles auth
                        textSearchFieldMask,
                        request
                );
                results.add(searchResult);
            } catch (Exception e) {
                log.error("Error performing text search for query: {}", request.getTextQuery(), e);
                results.add(JsonNodeFactory.instance.objectNode().put("error", e.getMessage()));
            }
        }
        return results;
    }
}
