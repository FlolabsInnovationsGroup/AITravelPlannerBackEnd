package com.springboot.FlomadAIplanner.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDTO {
    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId; // Associates the location with a chat/session

    @NotNull(message = "Latitude cannot be null")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90 degrees")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90 degrees")
    private Double latitude;

    @NotNull(message = "Longitude cannot be null")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180 degrees")  
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180 degrees")
    private Double longitude;

    @Size(max = 200, message = "Location name cannot exceed 200 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-.,()''&|?!:;/+#@$%\\u00C0-\\u017F]*$", message = "Location name contains invalid characters")
    private String name; // Optional name or description of the location

    @Size(max = 50, message = "Location type cannot exceed 50 characters")
    @Pattern(regexp = "^(attraction|cafe|hotel|restaurant|transport|park|museum|beach|mountain|landmark|shopping|entertainment|religious|hospital|airport|station|viewpoint|nature|sports|accommodation)$", message = "Location type must be one of: attraction, cafe, hotel, restaurant, transport, park, museum, beach, mountain, landmark, shopping, entertainment, religious, hospital, airport, station, viewpoint, nature, sports, accommodation")
    private String type; // E.g., "attraction", "cafe", "hotel", "park", "museum", "beach"
}
