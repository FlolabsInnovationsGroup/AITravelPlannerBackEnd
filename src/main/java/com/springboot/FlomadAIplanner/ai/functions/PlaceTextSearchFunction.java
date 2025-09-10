package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Function for searching place details using text queries.
 */
@Slf4j
@Component
@Description("""
        Searches for detailed place information using text queries. This function finds exact locations, coordinates, 
        and details for places like restaurants, hotels, landmarks, or addresses. Perfect for location verification, 
        getting coordinates, and finding detailed place information. Use when users ask about specific places or 
        need location details for trip planning. Can process multiple place queries efficiently in a single request.
        
        Examples: "Find details for Eiffel Tower", "Get coordinates for Central Park", "Search for Times Square info"
        """)

public class PlaceTextSearchFunction
//        implements AIFunction<PlaceTextSearchFunction.TextSearchRequestsWrapper, JsonNode>
{


    private final GooglePlacesServiceAsync googlePlacesService;
    private final RestTemplate restTemplate;

    @Value("${google.api.key}") // ✅ Reads API Key from application.properties
    private String googleApiKey;

    @Autowired
    public PlaceTextSearchFunction(GooglePlacesServiceAsync googlePlacesService, RestTemplate restTemplate) {
        this.googlePlacesService = googlePlacesService;
        this.restTemplate = restTemplate;
    }

    @Valid
    public JsonNode apply(TextSearchRequestsWrapper requestsWrapper) {
        log.info("🔎 Performing text search for {} queries...", requestsWrapper.getRequests().size());

        List<CompletableFuture<JsonNode>> searchFutures = new ArrayList<>();

        try {
            for (TextSearchRequest request : requestsWrapper.getRequests()) {
                searchFutures.add(googlePlacesService.asyncTextSearchPlaces(request));
            }

            // ✅ Collect results from async operations
            List<JsonNode> placeResults = searchFutures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            log.info("📡 Received {} place results from Google API.", placeResults.size());

            return JsonNodeFactory.instance.arrayNode().addAll(placeResults);

        } catch (Exception e) {
            log.error("❌ Error performing text search", e);
            return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextSearchRequestsWrapper {
        @JsonPropertyDescription("List of text search requests. Recommended to use for searching multiple places at once.")
        private List<TextSearchRequest> requests;
    }
}
