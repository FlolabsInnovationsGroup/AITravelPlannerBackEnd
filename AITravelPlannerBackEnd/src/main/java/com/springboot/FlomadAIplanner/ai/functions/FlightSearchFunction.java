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

@Slf4j
@Component
@RequiredArgsConstructor
@Description("""
        Searches for available flights based on the provided criteria.
        When using this function, ensure you provide IATA codes for the origin and destination airports.
        The customer may provide the city name, but you need to convert it to the IATA code before calling this function.
        If the customer provides only the departure time, it will be considered a one-way trip.
        If the customer provides a return date, it will be considered a round trip.
        You will need to call this function twice: once for the departure and once for the return.
        """)
public class FlightSearchFunction implements AIFunction<FlightSearchFunction.FlightSearchRequest, FlightSearchFunction.FlightSearchResponse> {

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
        private final List<FlightOffersPostRequest> requests;
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