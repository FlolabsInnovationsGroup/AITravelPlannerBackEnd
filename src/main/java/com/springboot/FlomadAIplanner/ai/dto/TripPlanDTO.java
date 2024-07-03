package com.springboot.FlomadAIplanner.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripPlanDTO {
    private String generalResponseMessage;
    private List<FlightSuggestion> toTripFlightSuggestions;
    private List<ApartmentBookingSuggestion> apartmentBookingSuggestions;
    private List<DayPlan> days;
    private List<FlightSuggestion> backFlightBookingSuggestions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlightSuggestion {
        private String flightNumber;
        private String flightDepartureTime;
        private String flightArrivalTime;
        private String flightAirline;
        private int flightPrice;
        private String linkToTheBookingPage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApartmentBookingSuggestion {
        private String apartmentName;
        private String apartmentAddress;
        private int apartmentPrice;
        private List<Integer> daysToStay;
        private String apartmentBookingPageLink;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayPlan {
        private int dayNumber;
        private List<Activity> activities;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Activity {
        private String activityName;
        private String activityDescription;
        private String activityAddress;
        private String activityImage;
    }
}
