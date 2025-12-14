package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackForwardGeocodingRequest;
import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackResponse.PositionStackData;
import com.springboot.FlomadAIplanner.exceptions.api.GoogleAPIException;
import com.springboot.FlomadAIplanner.service.google.GooglePlacesService;
import com.springboot.FlomadAIplanner.service.positionstack.PositionStackService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * Geocoding function that prioritizes PositionStack and falls back to Google Places API if needed.
 */
@Slf4j
@Component
@Description("""
        Uses PositionStack to find coordinates from a place name or address.
        If PositionStack fails, falls back to Google Places API.
        """)
public class PositionStackGeocodingFunction
        implements Function<PositionStackForwardGeocodingRequest, List<JsonNode>> {

    private final PositionStackService positionStackService;
    private final GooglePlacesService googlePlacesService;

    public PositionStackGeocodingFunction(PositionStackService positionStackService, GooglePlacesService googlePlacesService) {
        this.positionStackService = positionStackService;
        this.googlePlacesService = googlePlacesService;
    }

    @Override
    public List<JsonNode> apply(PositionStackForwardGeocodingRequest input) {
        log.info("🔍 Geocoding request for: {}", input.getQuery());

        // 🛠 Step 1: Try PositionStack
        List<PositionStackData> positionStackResults = positionStackService.bulkForwardGeocode(List.of(input));

        if (positionStackResults != null && !positionStackResults.isEmpty()) {
            log.info("✅ PositionStack returned {} results", positionStackResults.size());
            return List.of(JsonNodeFactory.instance.pojoNode(positionStackResults));
        }

        // 🚨 Step 2: Fallback to Google Places if PositionStack failed
        log.warn("⚠️ PositionStack returned no results, falling back to Google Places API.");

        TextSearchRequest googleRequest = new TextSearchRequest();
        googleRequest.setTextQuery(input.getQuery());

        try {
            JsonNode googleResponse = googlePlacesService.textSearchPlaces(googleRequest);
            if (googleResponse != null) {
                log.info("✅ Google Places returned geocoding results");
                return List.of(googleResponse);
            }
        } catch (GoogleAPIException e) {
            log.error("❌ Google Places API failed to fetch geocoding data", e);
        }

        // ❌ Step 3: If both fail, return an explicit error message
        log.error("❌ Geocoding failed: Both PositionStack and Google Places returned no results.");
        return List.of(JsonNodeFactory.instance.objectNode().put("error", "Geocoding failed: No results from PositionStack or Google Places."));
    }

    @Data
    @AllArgsConstructor
    @JsonClassDescription("""
        Wrapper for multiple PositionStack geocoding requests.
        JSON Schema of type: 'object'
        """)
    public static class PositionStackForwardGeocodingRequestsWrapper {

        @NotEmpty
        @Valid
        @JsonPropertyDescription("""
            REQUIRED. Contains a list of address queries for which we want lat/long data.
            MINIMUM one item.
            """)
        private List<PositionStackForwardGeocodingRequest> requests;
    }

}
