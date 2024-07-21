package com.springboot.FlomadAIplanner.dto.amadeus;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightOffersPostRequest {

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be a valid ISO 4217 code")
    @JsonPropertyDescription("REQUIRED. ISO 4217 currency code to reflect the currency in which this amount is expressed. Example: EUR")
    private String currencyCode;

    @NotEmpty
    @Size(min = 1, max = 6)
    @Valid
    @JsonPropertyDescription("REQUIRED. Min 1, Max 6 items. List of origin and destination details, must be properly ordered in time and within 365 days from today.")
    private List<OriginDestination> originDestinations;

    @NotEmpty
    @Size(min = 1, max = 18)
    @Valid
    @JsonPropertyDescription("REQUIRED. Min 1, Max 18 items. List of travelers in the trip. Maximum 9 passengers older than 2 years.")
    private List<Traveler> travelers;

    @NotEmpty
    @Size(min = 1)
    @JsonPropertyDescription("REQUIRED. List of sources. Specifies which sources to call for flight offers. Example: GDS")
    private List<@NotBlank String> sources;

    @Valid
    @JsonPropertyDescription("Optional. Additional search criteria to filter flight offers.")
    private SearchCriteria searchCriteria;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OriginDestination {

        @NotBlank
        @JsonPropertyDescription("REQUIRED. Unique identifier for this origin-destination pair. Example: 1")
        private String id;

        @NotBlank
        @JsonPropertyDescription("REQUIRED. Origin location code, must be a valid IATA code. Example: PAR")
        private String originLocationCode;

        @NotBlank
        @JsonPropertyDescription("REQUIRED. Destination location code, must be a valid IATA code. Example: NYC")
        private String destinationLocationCode;

        @Valid
        @NotNull
        @JsonPropertyDescription("REQUIRED. Date and time range for departure.")
        private DateTimeRange departureDateTimeRange;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DateTimeRange {

            @NotBlank
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in YYYY-MM-DD format")
            @JsonPropertyDescription("REQUIRED. Date in ISO 8601 format. Example: 2018-09-22")
            private String date;

            @Pattern(regexp = "\\d{2}:\\d{2}:\\d{2}", message = "Time must be in HH:mm:ss format")
            @JsonPropertyDescription("Optional. Time in local time, HH:mm:ss format. Example: 10:30:00")
            private String time;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Traveler {

        @NotBlank
        @JsonPropertyDescription("REQUIRED. Unique identifier for the traveler. Example: 1")
        private String id;

        @NotBlank
        @Pattern(regexp = "ADULT|CHILD|SENIOR|YOUNG|HELD_INFANT|SEATED_INFANT|STUDENT", message = "Invalid traveler type")
        @JsonPropertyDescription("REQUIRED. Traveler type. Enum: [ADULT, CHILD, SENIOR, YOUNG, HELD_INFANT, SEATED_INFANT, STUDENT]. Example: ADULT")
        private String travelerType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchCriteria {

        @Min(1)
        @Max(250)
        @JsonPropertyDescription("Optional. Max 250. Maximum number of flight offers to return. Default: 10. Example: 100")
        private Integer maxFlightOffers = 10;

        @Valid
        @JsonPropertyDescription("Optional. Additional filters for the flight offers.")
        private FlightFilters flightFilters;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class FlightFilters {

            @Valid
            @Size(min = 1, max = 6)
            @JsonPropertyDescription("Optional. Min 1, Max 6 items. Cabin restrictions for the flight offers.")
            private List<CabinRestriction> cabinRestrictions;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class CabinRestriction {

                @NotBlank
                @Pattern(regexp = "ECONOMY|PREMIUM_ECONOMY|BUSINESS|FIRST", message = "Invalid cabin type")
                @JsonPropertyDescription("REQUIRED. Cabin class. Enum: [ECONOMY, PREMIUM_ECONOMY, BUSINESS, FIRST]. Example: BUSINESS")
                private String cabin;

                @NotEmpty
                @JsonPropertyDescription("REQUIRED. List of origin-destination identifiers for which the cabin restriction applies. Example: [1, 2]")
                private List<@NotBlank String> originDestinationIds;

                @NotBlank
                @Pattern(regexp = "MOST_SEGMENTS|AT_LEAST_ONE_SEGMENT|ALL_SEGMENTS", message = "Invalid coverage type")
                @JsonPropertyDescription("REQUIRED. Part of the trip covered by the cabin restriction. Enum: [MOST_SEGMENTS, AT_LEAST_ONE_SEGMENT, ALL_SEGMENTS]. Example: MOST_SEGMENTS")
                private String coverage;
            }
        }
    }
}
