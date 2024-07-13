package com.springboot.FlomadAIplanner.ai.functions;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightOffersPostRequest;
import com.springboot.FlomadAIplanner.service.amadeus.AmadeusFlightsService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Description("""
    Searches for available flights based on the provided criteria. 
    When you are using this function, make sure to provide IATA codes for the origin and destination airports.
    The customer may provide the city name, but you need to convert it to the IATA code before calling this function.
    If the customer just provided the departure time, It will be considered as a one-way trip.
    If the customer provided the return date, It will be considered as a round trip.
    """)
public class FlightSearchFunction implements AIFunction<FlightSearchFunction.FlightSearchRequest, FlightSearchFunction.FlightSearchResponse> {

    private final AmadeusFlightsService amadeusFlightsService;

    private final ObjectMapper objectMapper;


    @Autowired
    public FlightSearchFunction(AmadeusFlightsService amadeusFlightsService,ObjectMapper objectMapper) {
        this.amadeusFlightsService = amadeusFlightsService;
        this.objectMapper = objectMapper;
    }


    @Override
    public FlightSearchResponse apply(FlightSearchRequest request) {
        log.info("Searching for flights from {} to {} on {} for {} passengers", request.getOrigin(), request.getDestination(), request.getDepartureDate(), request.getPassengers());

        List<FlightDetail> flights = new ArrayList<>();

        FlightOffersPostRequest departureRequest = createFlightOffersPostRequest(request.getOrigin(), request.getDestination(), request.getDepartureDate());
        try {
            JsonNode departureResponse = amadeusFlightsService.getFlightOffersPost(departureRequest);
            logJsonNode(departureResponse);
            flights.addAll(parseFlightSearchResponse(departureResponse));
        } catch (Exception e) {
            log.error("Error getting departure flight offers", e);
            throw new RuntimeException("Error getting departure flight offers", e);
        }

        if (Boolean.TRUE.equals(request.getRoundTrip()) && request.getReturnDate() != null) {
            log.info("Searching for return flights from {} to {} on {} for {} passengers", request.getDestination(), request.getOrigin(), request.getReturnDate(), request.getPassengers());
            FlightOffersPostRequest returnRequest = createFlightOffersPostRequest(request.getDestination(), request.getOrigin(), request.getReturnDate());
            try {
                JsonNode returnResponse = amadeusFlightsService.getFlightOffersPost(returnRequest);
                logJsonNode(returnResponse);
                flights.addAll(parseFlightSearchResponse(returnResponse));
            } catch (Exception e) {
                log.error("Error getting return flight offers", e);
                throw new RuntimeException("Error getting return flight offers", e);
            }
        }
        return new FlightSearchResponse(flights);
    }

    private List<FlightDetail> parseFlightSearchResponse(JsonNode root) {
        List<FlightDetail> flights = new ArrayList<>();
        JsonNode dataArray = root.path("data");

        for (JsonNode dataNode : dataArray) {
            JsonNode itineraries = dataNode.path("itineraries");
            JsonNode priceNode = dataNode.path("price");
            String price = priceNode.path("total").asText();

            for (JsonNode itinerary : itineraries) {
                JsonNode segments = itinerary.path("segments");
                for (JsonNode segment : segments) {
                    String flightNumber = segment.path("carrierCode").asText() + " " + segment.path("number").asText();
                    String departureTime = segment.path("departure").path("at").asText();
                    String arrivalTime = segment.path("arrival").path("at").asText();
                    String airline = root.path("dictionaries").path("carriers").path(segment.path("carrierCode").asText()).asText();
                    String bookingLink = "https://booking.example.com";

                    flights.add(new FlightDetail(flightNumber, departureTime, arrivalTime, airline, Double.parseDouble(price), bookingLink));
                }
            }
        }

        return flights;
    }

    private FlightOffersPostRequest createFlightOffersPostRequest(String origin, String destination, String date) {
        return new FlightOffersPostRequest(
            "USD",
            List.of(new FlightOffersPostRequest.OriginDestination("1", origin, destination,
                    new FlightOffersPostRequest.OriginDestination.DateTimeRange(date, null))),
            List.of(new FlightOffersPostRequest.Traveler("1", "ADULT")),
            List.of("GDS"),
            new FlightOffersPostRequest.SearchCriteria(1, null)
        );
    }

    private void logJsonNode(JsonNode response) {
        try {
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
            log.info("Received response from Amadeus: \n{}", jsonString);
        } catch (Exception e) {
            log.error("Error converting JsonNode to String", e);
        }
    }
    
    @Data
    public static class FlightSearchRequest {
        private String origin;
        private String destination;
        private String departureDate;
        private String returnDate;
        private int passengers;
        private Boolean roundTrip;
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