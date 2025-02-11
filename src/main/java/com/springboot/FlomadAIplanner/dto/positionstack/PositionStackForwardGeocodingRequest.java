package com.springboot.FlomadAIplanner.dto.positionstack;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines a single address query for forward geocoding via PositionStack.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionStackForwardGeocodingRequest {

    @NotBlank
    @JsonPropertyDescription("""
            REQUIRED. The textual address or place name to geocode.
            Example: "1600 Pennsylvania Ave NW, Washington DC" or just "Paris"
            """)
    private String query;

    @Min(1)
    @JsonPropertyDescription("""
            OPTIONAL. Specifies how many results to return for this single query.
            Allowed range is 1 to 80. Default is 5 if not provided.
            """)
    private Integer limit = 5;

    @JsonPropertyDescription("""
            OPTIONAL. Filter results by 2-letter or 3-letter country code.
            Example: "US" or "GBR" to narrow down the search to that country only.
            """)
    private String country;

    @JsonPropertyDescription("""
            OPTIONAL. Filter by region, city, or state.
            Example: "Berlin" for region=Berlin, or "CA" for region=California (USA).
            """)
    private String region;

    @JsonPropertyDescription("""
            OPTIONAL. Desired output format. Usually "json" is the default.
            You can leave it blank to use the default.
            """)
    private String output;
}
