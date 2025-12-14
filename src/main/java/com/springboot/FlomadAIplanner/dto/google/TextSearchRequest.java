package com.springboot.FlomadAIplanner.dto.google;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Only include non-null fields in JSON
public class TextSearchRequest {

    @NotBlank
    @JsonPropertyDescription("REQUIRED. The exact place name used to search for the more detailed information about the location of the required place." +
            "CRITICAL: ALWAYS include the city name or region to avoid wrong locations." +
            "CORRECT: 'Hidden Gems, Paris' | WRONG: 'Hidden Gems' (finds New York instead!)" +
            "CORRECT: 'Eiffel Tower, Paris' | WRONG: 'Eiffel Tower' (ambiguous)" +
            "Example formats: 'Louvre Museum, Paris', 'Colosseum, Rome', 'Sagrada Familia, Barcelona'.")
    private String textQuery;
    
    @JsonProperty("locationBias")
    @JsonPropertyDescription("OPTIONAL. Geographic bias to restrict search results to a specific area. " +
            "Provide as LocationBias object with circle containing center (latitude, longitude) and radius in meters. " +
            "Example: {\"circle\": {\"center\": {\"latitude\": 48.8566, \"longitude\": 2.3522}, \"radius\": 50000}} " +
            "This restricts search to 50km around Paris center. " +
            "Use when planning trips to specific cities to prevent finding wrong locations worldwide.")
    private LocationBiasDTO locationBias;
    
    // Nested DTO for location bias structure
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LocationBiasDTO {
        private CircleDTO circle;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CircleDTO {
            private CoordinateDTO center;
            private Double radius;
            
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class CoordinateDTO {
                private Double latitude;
                private Double longitude;
            }
        }
    }

}
