package com.springboot.FlomadAIplanner.dto.positionstack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

import java.util.List;

/**
 * Represents the JSON response from PositionStack /forward endpoint.
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionStackResponse {

    @JsonPropertyDescription("List of location results returned by the PositionStack API for a single query.")
    private List<PositionStackData> data;

    // You can add additional fields if needed (e.g., 'error', 'code', etc.).

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PositionStackData {

        @JsonPropertyDescription("Latitude coordinate for the location result.")
        private Double latitude;

        @JsonPropertyDescription("Longitude coordinate for the location result.")
        private Double longitude;

        @JsonPropertyDescription("Full display label (like '1600 Pennsylvania Avenue NW, Washington, DC, USA').")
        private String label;

        @JsonPropertyDescription("Confidence score from 0..1 representing how sure the service is about this location.")
        private Double confidence;

        @JsonPropertyDescription("The name or street number (if it's an address).")
        private String name;

        @JsonPropertyDescription("The type of the result (e.g., 'address', 'venue', 'city').")
        private String type;

        @JsonPropertyDescription("The region or state name (e.g., 'District of Columbia').")
        private String region;

        @JsonPropertyDescription("The ISO 3166-1 alpha-2 or alpha-3 country code.")
        private String country_code;

        // Additional fields can be added as needed.
    }
}
