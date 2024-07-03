package com.springboot.FlomadAIplanner.ai.functions;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Description("Searches for available flights based on the provided criteria.")
public class FlightSearchFunctionMock implements AIFunction<FlightSearchFunctionMock.FlightSearchRequest, FlightSearchFunctionMock.FlightSearchResponse> {

    @Override
    public FlightSearchResponse apply(FlightSearchRequest request) {
        log.info("Searching for flights from {} to {} on {} for {} passengers", request.getOrigin(), request.getDestination(), request.getDepartureDate(), request.getPassengers());
        // Mock implementation
        return new FlightSearchResponse(List.of(
                new FlightDetail("AF 123", "2021-12-01T10:00:00", "2021-12-01T12:00:00", "Air France", 200, "https://www.airfrance.com"),
                new FlightDetail("BA 456", "2021-12-01T14:00:00", "2021-12-01T16:00:00", "British Airways", 250, "https://www.britishairways.com")
        ));
    }

    @Data
    public static class FlightSearchRequest {
        private String origin;
        private String destination;
        private String departureDate;
        private String returnDate;
        private int passengers;
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
