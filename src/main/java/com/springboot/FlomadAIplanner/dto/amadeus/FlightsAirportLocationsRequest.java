package com.springboot.FlomadAIplanner.dto.amadeus;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightsAirportLocationsRequest {

    @NotBlank
    @JsonPropertyDescription("Required. Sub type of the location: AIRPORT or CITY")
    private String subType;

    @NotBlank
    @JsonPropertyDescription("Required. Keyword representing the start of a word in a city or airport name or code")
    private String keyword;

    @JsonPropertyDescription("Optional. Country code in ISO 3166-1 alpha-2 format, e.g., US")
    private String countryCode;

    @JsonPropertyDescription("Optional. Max items per page. Default: 10")
    private Integer pageLimit;

    @JsonPropertyDescription("Optional. Start index of the requested page. Default: 0")
    private Integer pageOffset;

    @JsonPropertyDescription("Optional. Attribute for sorting: analytics.travelers.score. Default: analytics.travelers.score")
    private String sort;

    @JsonPropertyDescription("Optional. Level of information: LIGHT or FULL. Default: FULL")
    private String view;
}
