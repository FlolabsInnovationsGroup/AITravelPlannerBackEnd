package com.springboot.FlomadAIplanner.ai.functions;


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
        When you are using this function, make sure to provide IATA codes for the origin and destination airports.
        The customer may provide the city name, but you need to convert it to the IATA code before calling this function.
        If the customer just provided the departure time, It will be considered as a one-way trip.
        If the customer provided the return date, It will be considered as a round trip.
        And that means you need to call this function twice, once for the departure and once for the return.
        """)
public class FlightSearchFunction implements AIFunction<FlightOffersPostRequest, FlightSearchFunction.FlightSearchResponse> {

    private final AmadeusFlightsService amadeusFlightsService;
    private final Converter<FlightOffersPostResponse, FlightSearchResponse> fromAPIToResponseConverter;


    @Override
    public FlightSearchResponse apply(FlightOffersPostRequest request) {
        log.info("Searching for available flights information.");

        List<FlightDetail> flights = new ArrayList<>();
        try {
            FlightOffersPostResponse departureResponse = amadeusFlightsService.getFlightOffersPost(request);
            flights = fromAPIToResponseConverter.convert(departureResponse).getFlights();
        } catch (Exception e) {
            log.error("Error getting departure flight offers", e);
            flights.add(new FlightDetail("Error", "Error", "Error", "Error", 0, ""));
        }

        log.info("FlightsSearchFunction: Found {} flights. Response length: {}", flights.size(), flights.toString().length());
        return new FlightSearchResponse(flights);
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