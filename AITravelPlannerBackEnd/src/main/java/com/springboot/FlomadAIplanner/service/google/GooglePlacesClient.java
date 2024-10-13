package com.springboot.FlomadAIplanner.service.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import com.springboot.FlomadAIplanner.service.google.confg.GooglePlacesClientConfig;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for interacting with the Google Places API.
 * <p>
 * This interface provides a method to search for nearby places using Google Places API.
 * It leverages Feign to simplify making API calls and automatically handles aspects like
 * URL construction, request body serialization, and response deserialization.
 */
@FeignClient(name = "googlePlacesClient", url = "${google.api.baseurl}",
        configuration = GooglePlacesClientConfig.class)
public interface GooglePlacesClient {

    /**
     * Searches for nearby places based on the provided request parameters.
     * <p>
     * This method sends a POST request to the Google Places APIs `/v1/places:searchNearby` endpoint.
     *
     * @param fieldMask (Optional) A comma-separated list of fields to restrict in the response.
     *                  Refer to Google Places API documentation for available fields.
     *                  If omitted, all available fields will be returned.
     * @param request   A {@link NearbyPlacesRequest} object containing search parameters.
     *                  This includes location coordinates (latitude, longitude), radius,
     *                  and optional filters like keyword or place type.
     * @return A JSON response containing the search results, including details of nearby places.
     * The specific structure of the response depends on the requested fields.
     * @throws FeignException If an error occurs during the API call. Refer to Feign documentation
     *                        for details on potential exceptions.
     *                        <p>
     * @description <a href="https://developers.google.com/maps/documentation/places/web-service/nearby-search">...</a>
     */
    @PostMapping(value = "/v1/places:searchNearby", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode searchNearbyPlaces(
            @RequestHeader("X-Goog-FieldMask") String fieldMask,
            @RequestBody NearbyPlacesRequest request);

    /**
     * Searches for places based on the provided text query.
     * <p>
     * This method sends a POST request to the Google Places APIs `/v1/places:searchText` endpoint.
     *
     * @param fieldMask A comma-separated list of fields to request in the response.
     * @param request   A {@link TextSearchRequest} object containing the text query to search for.
     *                  <p>
     * @description <a href="https://developers.google.com/maps/documentation/places/web-service/text-search">...</a>
     */
    @PostMapping(value = "/v1/places:searchText", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode textSearchPlaces(
            @RequestHeader("X-Goog-FieldMask") String fieldMask,
            @RequestBody TextSearchRequest request);
}
