package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.dto.amadeus.HotelSearchByGeocodeRequest;
import com.springboot.FlomadAIplanner.dto.amadeus.HotelSearchByGeocodeResponse;
import com.springboot.FlomadAIplanner.exceptions.api.AmadeusAPIException;
import com.springboot.FlomadAIplanner.service.amadeus.AmadeusHotelsSearchService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
@Description("""
        Searches for hotels based on geographic location coordinates. This function finds hotels near specific 
        latitude/longitude coordinates with detailed information including amenities, ratings, and pricing.
        
        WORKFLOW: First use PlaceTextSearchFunction to get exact coordinates, then use this function to find nearby hotels.
        
        Features:
        - Geographic precision using latitude/longitude coordinates
        - Filter by proximity, amenities, ratings, and hotel sources  
        - Detailed hotel information including facilities and pricing
        - Integration with multiple hotel booking sources
        
        IMPORTANT: After finding hotels, use PlaceTextSearchFunction for each hotel to get exact locations and Google Maps links.
        
        Use when users search for hotels in specific areas, need accommodation near landmarks, or want location-based hotel recommendations.
        
        Examples: "Find hotels near Central Park", "Show me hotels in downtown Paris", "Hotels within 5km of airport"
        """)
public class HotelsSearchByLocationFunction implements Function<HotelsSearchByLocationFunction.HotelSearchRequestWrapper, List<HotelSearchByGeocodeResponse>> {

    private final AmadeusHotelsSearchService amadeusHotelsSearchService;

    public HotelsSearchByLocationFunction(AmadeusHotelsSearchService amadeusHotelsSearchService) {
        this.amadeusHotelsSearchService = amadeusHotelsSearchService;
    }

    @Override
    @Valid
    public List<HotelSearchByGeocodeResponse> apply(HotelSearchRequestWrapper requestWrapper) {
        log.info("Initiating geocode-based hotel searches.");
        List<HotelSearchByGeocodeResponse> combinedResponses = new ArrayList<>();

        for (HotelSearchByGeocodeRequest request : requestWrapper.getRequests()) {
            log.info("Searching hotels by geocode at latitude: {}, longitude: {}", request.getLatitude(), request.getLongitude());
            try {
                HotelSearchByGeocodeResponse response = amadeusHotelsSearchService.searchHotelsByGeocode(request);
                log.info("Received hotel search response from Amadeus. size: {};  Response length: {}", response.getData().size(), response.toString().length());
                combinedResponses.add(response);
            } catch (AmadeusAPIException e) {
                log.error("Error searching for hotels by geocode", e);
                HotelSearchByGeocodeResponse errorResponse = HotelSearchByGeocodeResponse.builder()
                        .errorMessage("Error for latitude " + request.getLatitude() + ", longitude " + request.getLongitude() + ": " + e.getMessage())
                        .build();
                combinedResponses.add(errorResponse);
            }
        }

        log.info("Combined search results for all provided geocode requests.");
        return combinedResponses;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotelSearchRequestWrapper {

        @Valid
        @JsonPropertyDescription("List of geocode-based hotel search requests containing latitude, longitude, and optional filters.")
        private List<HotelSearchByGeocodeRequest> requests;
    }
}
