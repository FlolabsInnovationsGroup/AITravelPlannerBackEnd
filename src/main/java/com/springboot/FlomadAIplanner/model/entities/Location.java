package com.springboot.FlomadAIplanner.model.entities;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "locations")
public class Location extends Model {
    
    private static final long serialVersionUID = 1L;
    
    @Field("sessionId")
    @NotBlank
    private String sessionId; // Associates the location with a chat/session

    @Field("latitude")
    @NotNull(message = "Latitude cannot be null")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90 degrees")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90 degrees")
    private Double latitude;

    @Field("longitude")
    @NotNull(message = "Longitude cannot be null")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180 degrees")  
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180 degrees")
    private Double longitude;

    @Field("name")
    @Size(max = 200, message = "Location name cannot exceed 200 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-.,()\\u00C0-\\u017F]*$", message = "Location name contains invalid characters")
    private String name; // Optional name or description of the location

    @Field("type")
    @Size(max = 50, message = "Location type cannot exceed 50 characters")
    @Pattern(regexp = "^(attraction|cafe|hotel|restaurant|transport|park|museum|beach|mountain|landmark|shopping|entertainment|religious|hospital|airport|station|viewpoint|nature|sports|accommodation)$", message = "Location type must be one of: attraction, cafe, hotel, restaurant, transport, park, museum, beach, mountain, landmark, shopping, entertainment, religious, hospital, airport, station, viewpoint, nature, sports, accommodation")
    private String type; // E.g., "attraction", "cafe", "hotel", "park", "museum", "beach"

    @Field("createdAt")
    private LocalDateTime createdAt;

    @Field("updatedAt")
    private LocalDateTime updatedAt;
}
