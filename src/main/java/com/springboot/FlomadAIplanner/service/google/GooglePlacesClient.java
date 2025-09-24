package com.springboot.FlomadAIplanner.service.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import com.springboot.FlomadAIplanner.service.google.confg.GooglePlacesClientConfig;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

/**
 * Feign client for interacting with the Google Places API.
 *
 * This interface provides methods to search for nearby places and text search using Google Places API.
 * It leverages Feign to simplify making API calls and automatically handles aspects like
 * URL construction, request body serialization, and response deserialization.
 */
@FeignClient(name = "googlePlacesClient", url = "${google.api.baseurl}",
        configuration = GooglePlacesClientConfig.class)
public interface GooglePlacesClient {

    /**
     * Searches for nearby places based on the provided request parameters.
     *
     * This method sends a POST request to the Google Places APIs `/v1/places:searchNearby` endpoint.
     *
     * @param apiKey    The API key for authentication, passed as a Bearer token in the Authorization header.
     * @param fieldMask (Optional) A comma-separated list of fields to restrict in the response.
     *                  Refer to Google Places API documentation for available fields.
     *                  If omitted, all available fields will be returned.
     * @param request   A {@link NearbyPlacesRequest} object containing search parameters.
     *                  This includes location coordinates (latitude, longitude), radius,
     *                  and optional filters like keyword or place type.
     * @return A JSON response containing the search results, including details of nearby places.
     * The specific structure of the response depends on the requested fields.
     * @throws FeignException If an error occurs during the API call.
     */
    @GetMapping("/nearbysearch/json")
    JsonNode searchNearbyPlaces(
            @RequestParam("key") String apiKey,
            @RequestParam("location") String location,
            @RequestParam("radius") int radius,
            @RequestParam("type") String type,
            @RequestParam(value = "fields", required = false) String fieldMask
    );


    /**
     * Searches for places based on the provided text query.
     *
     * This method sends a POST request to the Google Places APIs `/v1/places:searchText` endpoint.
     *
     * @param apiKey    The API key for authentication, passed as a Bearer token in the Authorization header.
     * @param fieldMask A comma-separated list of fields to request in the response.
     * @param request   A {@link TextSearchRequest} object containing the text query to search for.
     * @return A JSON response containing place search results based on text input.
     */
    @PostMapping(value = "/v1/places:searchText", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode textSearchPlaces(
            @RequestHeader("Authorization") String apiKey,
            @RequestHeader("X-Goog-FieldMask") String fieldMask,
            @RequestBody TextSearchRequest request);
}