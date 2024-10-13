package com.springboot.FlomadAIplanner.dto.amadeus;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightDestinationsRequest {
    @NotBlank
    @JsonPropertyDescription("Required. IATA code of the departure city, e.g., MAD for Madrid")
    private String origin;

    @JsonPropertyDescription("Optional. Departure date or range in YYYY-MM-DD format, e.g., 2017-12-25")
    private String departureDate;

    @JsonPropertyDescription("Optional. Default: false. If true, only one-way flights are considered")
    private Boolean oneWay;

    @JsonPropertyDescription("Optional. Duration or range in days, e.g., 2,8")
    private String duration;

    @JsonPropertyDescription("Optional. Default: false. If true, only non-stop flights are considered")
    private Boolean nonStop;

    @JsonPropertyDescription("Optional. Positive number. Max price for each offer")
    private Integer maxPrice;

    @JsonPropertyDescription("Optional. View by DATE, DESTINATION, DURATION, WEEK, or COUNTRY")
    private String viewBy;
}
