package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackForwardGeocodingRequest;
import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackResponse.PositionStackData;
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
 * Replaces the old Google-based place text search with a PositionStack-based geocoding function.
 * <p>
 * Purpose:
 * - Use this function whenever you need to convert textual place names or addresses into
 * latitude/longitude coordinates.
 * - Supports batch requests: send multiple addresses in a single call for efficiency.
 * - If you only need lat/long data for user-provided addresses, call this function instead
 * of more expensive, specialized Google APIs.
 * <p>
 * Typical Usage Scenarios:
 * - Searching for location coordinates to show on a map or to do further distance calculations.
 * - Geocoding addresses from user input (e.g., "Eiffel Tower, Paris") to lat/long.
 * - Any chat flow that references addresses but needs real location data.
 * <p>
 * Output:
 * - A List of PositionStackData items, each containing lat/long and optional metadata
 * (such as 'label', 'region', 'confidence', etc.).
 */
@Slf4j
@Component
@Description("""
        Input Schema must be a JSON Schema of 'type: \\"object\\"', not 'type: \\"None\\"
        
        Purpose:
        Use this function whenever you need to find exact coordinates (latitude and longitude) from text place address.
        Use it to find one geocoding address in one call.
        
        Returns:
        Latitude and Longitude coordinates for each address.
        """)
// Note: Right now batch is not working properly, so we only support single address queries for now

public class PositionStackGeocodingFunction
        implements Function<PositionStackForwardGeocodingRequest, List<PositionStackData>> {

    private final PositionStackService positionStackService;

    // Constructor-based dependency injection
    public PositionStackGeocodingFunction(PositionStackService positionStackService) {
        this.positionStackService = positionStackService;
    }

    /**
     * Processes the user input of multiple address queries, returns a single list
     * of geocoding results (lat/long) for each address.
     */
    @Override
    public List<PositionStackData> apply(PositionStackForwardGeocodingRequest input) {
        PositionStackForwardGeocodingRequestsWrapper wrapper = new PositionStackForwardGeocodingRequestsWrapper(List.of(input));
        log.info("PositionStackGeocodingFunction called with {} queries", wrapper.getRequests().size());
        return positionStackService.bulkForwardGeocode(wrapper);
    }

    /**
     * This is the top-level input object that the LLM will parse
     * and pass into the function.
     */
    @Data
    @AllArgsConstructor
    @JsonClassDescription("""
            Wrapper for the PositionStack geocoding requests.
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
