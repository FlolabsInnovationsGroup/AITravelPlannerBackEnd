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
    
    private static final long serialVersionUID = -4827394756291847362L;
    
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
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-.,()''&|?!:;/+#@$%\\u00C0-\\u024F\\u1E00-\\u1EFF]*$", message = "Location name contains invalid characters")
    private String name; // Optional name or description of the location

    @Field("type")
    @Size(max = 50, message = "Location type cannot exceed 50 characters")
    @Pattern(regexp = "^(attraction|cafe|hotel|restaurant|transport|park|museum|beach|mountain|landmark|shopping|entertainment|religious|hospital|airport|station|viewpoint|nature|sports|accommodation)$", message = "Location type must be one of: attraction, cafe, hotel, restaurant, transport, park, museum, beach, mountain, landmark, shopping, entertainment, religious, hospital, airport, station, viewpoint, nature, sports, accommodation")
    private String type; // E.g., "attraction", "cafe", "hotel", "park", "museum", "beach"

    // Enhanced information for rich popups
    @Field("photos")
    private java.util.List<String> photos; // Photo URLs from Google Places API

    @Field("rating")
    @DecimalMin(value = "0.0", message = "Rating must be between 0 and 5")
    @DecimalMax(value = "5.0", message = "Rating must be between 0 and 5")
    private Double rating; // Google Places rating (0-5 scale)

    @Field("userRatingsTotal")
    @Min(value = 0, message = "User ratings total must be non-negative")
    private Integer userRatingsTotal; // Number of reviews

    @Field("priceLevel")
    @Min(value = 1, message = "Price level must be between 1 and 4")
    @Max(value = 4, message = "Price level must be between 1 and 4")
    private Integer priceLevel; // Google Places price level (1-4 scale)

    @Field("phoneNumber")
    @Size(max = 50, message = "Phone number cannot exceed 50 characters")
    private String phoneNumber; // Business phone number

    @Field("website")
    @Size(max = 500, message = "Website URL cannot exceed 500 characters")
    private String website; // Business website URL

    @Field("vicinity")
    @Size(max = 200, message = "Vicinity cannot exceed 200 characters")
    private String vicinity; // Business address or vicinity

    @Field("placeId")
    @Size(max = 100, message = "Place ID cannot exceed 100 characters")
    private String placeId; // Google Places ID for detailed lookups

    @Field("isOpen")
    private Boolean isOpen; // Current open status (if available)

    @Field("weekdayText")
    private java.util.List<String> weekdayText; // Opening hours text

    @Field("createdAt")
    private LocalDateTime createdAt;

    @Field("updatedAt")
    private LocalDateTime updatedAt;
}
