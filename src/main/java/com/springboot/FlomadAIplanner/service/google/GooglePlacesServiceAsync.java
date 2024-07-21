package com.springboot.FlomadAIplanner.service.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import com.springboot.FlomadAIplanner.exceptions.api.GoogleAPIException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service class for handling Google Places API interactions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GooglePlacesServiceAsync {

    private final GooglePlacesClient googlePlacesClient;

    @Value("${google.places.fieldMask}")
    private String placesFieldMask;

    @Value("${google.textSearch.fieldMask}")
    private String textSearchFieldMask;

    /**
     * Asynchronously performs a search for nearby places using the provided request.
     *
     * @param request The request object containing search parameters.
     * @return a CompletableFuture containing the JSON response from the search
     */
    @Async
    @SuppressWarnings("unused")
    protected CompletableFuture<JsonNode> searchNearbyPlaces(NearbyPlacesRequest request) {
        log.info("Searching Google nearby places with request: {}", request);
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonNode response = googlePlacesClient.searchNearbyPlaces(placesFieldMask, request);
                log.info("Received response from Google Places API: {}", response);
                return response;
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
     * @return a CompletableFuture containing the JSON response from the search
     */
    @Async
    protected CompletableFuture<JsonNode> asyncTextSearchPlaces(TextSearchRequest request) {
        log.info("Searching Google places with request: {}", request);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return googlePlacesClient.textSearchPlaces(textSearchFieldMask, request);
            } catch (Exception e) {
                log.error("Error performing text search for query: {}", request.getTextQuery(), e);
                return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
            }
        });
    }

    /**
     * Performs bulk text searches for multiple queries and aggregates the results.
     *
     * @param requests The list of requests containing search parameters.
     * @return a JSON array node containing the aggregated search results.
     * @throws GoogleAPIException If an error occurs while calling the Google Places API.
     */
    public JsonNode bulkTextSearchPlaces(List<TextSearchRequest> requests) throws GoogleAPIException {
        log.info("Performing bulk text search with {} queries", requests);

        // Initiate asynchronous text searches
        List<CompletableFuture<JsonNode>> futures = initiateAsyncTextSearches(requests);

        // Collect results from the asynchronous text searches
        return collectSearchResults(futures);
    }

    /**
     * Initiates asynchronous text searches for each request in the list.
     *
     * @param requests The list of text search requests.
     * @return a list of CompletableFuture objects, each representing an asynchronous search result.
     */
    private List<CompletableFuture<JsonNode>> initiateAsyncTextSearches(List<TextSearchRequest> requests) {
        return requests.stream()
                .map(this::asyncTextSearchPlaces)
                .collect(Collectors.toList());
    }

    /**
     * Collects the results from the asynchronous search futures and aggregates them into a JSON array node.
     *
     * @param futures A list of CompletableFuture objects, each representing an asynchronous search result.
     * @return a JSON array node containing the aggregated search results.
     */
    private ArrayNode collectSearchResults(List<CompletableFuture<JsonNode>> futures) {
        ArrayNode results = JsonNodeFactory.instance.arrayNode();

        // Ensure all async operations complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Collect results from each future
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
