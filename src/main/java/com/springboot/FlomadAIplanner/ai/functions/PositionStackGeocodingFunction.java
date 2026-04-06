package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackForwardGeocodingRequest;
import com.springboot.FlomadAIplanner.service.geoapify.GeoapifyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
@Description("""
        Uses Geoapify to find coordinates from a place name or address.
        """)
public class PositionStackGeocodingFunction
        implements Function<PositionStackForwardGeocodingRequest, List<JsonNode>> {

    private final GeoapifyService geoapifyService;

    public PositionStackGeocodingFunction(GeoapifyService geoapifyService) {
        this.geoapifyService = geoapifyService;
    }

    @Override
    public List<JsonNode> apply(PositionStackForwardGeocodingRequest input) {
        log.info("Geocoding request for: {}", input.getQuery());

        try {
            JsonNode geoapifyResult = geoapifyService.geocode(input.getQuery());

            if (geoapifyResult != null && !geoapifyResult.has("error")) {
                log.info("Geoapify returned geocoding results");
                return List.of(geoapifyResult);
            }

            log.error("Geoapify geocoding returned no results for: {}", input.getQuery());
        } catch (Exception e) {
            log.error("Geoapify geocoding failed: {}", e.getMessage(), e);
        }

        return List.of(JsonNodeFactory.instance.objectNode()
                .put("error", "Geocoding failed: No results from Geoapify for query: " + input.getQuery()));
    }

    @Data
    @AllArgsConstructor
    @JsonClassDescription("""
        Wrapper for multiple geocoding requests.
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
