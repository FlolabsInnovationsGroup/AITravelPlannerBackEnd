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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Description("""
        Use this function to perform geocode-based searches for hotels.
        This function accepts longitude and latitude details and other optional parameters to find hotels.
        Before calling this function, use the "PlaceTextSearchFunction" to get the exact location (longitude and latitude) of a required place.
        Geocode search allows specifying exact geographical points using latitude and longitude, and filters based on proximity, amenities, ratings, and hotel sources.
        This method provides a way to discover hotels that are precisely tailored to geographical and preference-based needs.
        To get the exact location (Google Maps link) for each of the found hotels, you must use the PlaceTextSearchFunction for every found hotel separately.""")
public class HotelsSearchByLocationFunction implements AIFunction<HotelsSearchByLocationFunction.HotelSearchRequestWrapper, List<HotelSearchByGeocodeResponse>> {

    private final AmadeusHotelsSearchService amadeusHotelsSearchService;

    @Autowired
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
