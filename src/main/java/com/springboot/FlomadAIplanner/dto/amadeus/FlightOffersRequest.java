package com.springboot.FlomadAIplanner.dto.amadeus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightOffersRequest {

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be a valid ISO 4217 code")
    private String currencyCode;

    @NotEmpty
    @Size(min = 1, max = 6)
    @Valid
    private List<OriginDestination> originDestinations;

    @NotEmpty
    @Size(min = 1, max = 18)
    @Valid
    private List<Traveler> travelers;

    @NotEmpty
    @Size(min = 1)
    private List<@NotBlank String> sources;

    @Valid
    private SearchCriteria searchCriteria;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OriginDestination {

        @NotBlank
        private String id;

        @NotBlank
        private String originLocationCode;

        @NotBlank
        private String destinationLocationCode;

        @Valid
        @NotNull
        private DateTimeRange departureDateTimeRange;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DateTimeRange {

            @NotBlank
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in YYYY-MM-DD format")
            private String date;

            @Pattern(regexp = "\\d{2}:\\d{2}:\\d{2}", message = "Time must be in HH:mm:ss format")
            private String time;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Traveler {

        @NotBlank
        private String id;

        @NotBlank
        @Pattern(regexp = "ADULT|CHILD|SENIOR|YOUNG|HELD_INFANT|SEATED_INFANT|STUDENT", message = "Invalid traveler type")
        private String travelerType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchCriteria {

        @Min(1)
        @Max(250)
        private Integer maxFlightOffers;

        @Valid
        private FlightFilters flightFilters;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class FlightFilters {

            @Valid
            @Size(min = 1, max = 6)
            private List<CabinRestriction> cabinRestrictions;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class CabinRestriction {

                @NotBlank
                @Pattern(regexp = "ECONOMY|PREMIUM_ECONOMY|BUSINESS|FIRST", message = "Invalid cabin type")
                private String cabin;

                @NotEmpty
                private List<@NotBlank String> originDestinationIds;

                @NotBlank
                @Pattern(regexp = "MOST_SEGMENTS|AT_LEAST_ONE_SEGMENT|ALL_SEGMENTS", message = "Invalid coverage type")
                private String coverage;
            }
        }
    }
}
