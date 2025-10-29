package com.springboot.FlomadAIplanner.ai.functions;


import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.converter.Converter;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightOffersPostRequest;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightOffersPostResponse;
import com.springboot.FlomadAIplanner.service.amadeus.AmadeusFlightsService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component("flightSearchFunction")
@RequiredArgsConstructor
@Description("""
        Searches for available flight options and pricing based on travel criteria. This function provides comprehensive 
        flight information including airlines, departure/arrival times, durations, prices, and booking details.

        IMPORTANT: Always use IATA airport codes (e.g., LAX for Los Angeles, JFK for New York). Convert city names
        to airport codes before calling this function.

        For ONE-WAY trips: Provide departure date only
        For ROUND-TRIP: Provide both departure and return dates

        Returns detailed flight information to help users make informed booking decisions. Use this when users search
        for flights, ask about flight prices, or need travel options between cities.

        Examples: "Find flights from NYC to Paris", "Show me flight options to Tokyo next month"
        """)
public class FlightSearchFunction implements Function<FlightSearchFunction.FlightSearchRequest, FlightSearchFunction.FlightSearchResponse> {

    private final AmadeusFlightsService amadeusFlightsService;
    private final Converter<FlightOffersPostResponse, FlightSearchResponse> fromAPIToResponseConverter;

    @Override
    public FlightSearchResponse apply(FlightSearchRequest request) {
        log.info("Searching for available flights information.");

        List<FlightDetail> flights = new ArrayList<>();
        try {
            for (FlightOffersPostRequest flightRequest : request.getRequests()) {
                FlightOffersPostResponse response = amadeusFlightsService.getFlightOffersPost(flightRequest);
                flights.addAll(fromAPIToResponseConverter.convert(response).getFlights());
            }
        } catch (Exception e) {
            log.error("Error getting flight offers", e);
            flights.add(new FlightDetail("Error", "Error", "Error", "Error", 0, ""));
        }

        log.info("FlightSearchFunction: Found {} flights. Response length: {}", flights.size(), flights.toString().length());
        return new FlightSearchResponse(flights);
    }

    @Data
    public static class FlightSearchRequest {
        @JsonPropertyDescription("A list of flight offer requests, each containing the criteria for searching flights, such as origin, destination, and dates.")
        private List<FlightOffersPostRequest> requests;
        
        // No-args constructor for Jackson
        public FlightSearchRequest() {}
        
        // Constructor with parameters
        public FlightSearchRequest(List<FlightOffersPostRequest> requests) {
            this.requests = requests;
        }
    }

    @Data
    public static class FlightSearchResponse {
        private final List<FlightDetail> flights;
    }

    @Data
    public static class FlightDetail {
        private final String flightNumber;
        private final String departureTime;
        private final String arrivalTime;
        private final String airline;
        private final double price;
        private final String bookingLink;
    }
}