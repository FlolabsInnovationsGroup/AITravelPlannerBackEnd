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

    /** Serial version UID. */
    private static final long serialVersionUID = -4827394756291847362L;

    /** Max length for location name. */
    private static final int MAX_NAME_LENGTH = 200;
    /** Max length for location type string. */
    private static final int MAX_TYPE_LENGTH = 50;
    /** Max length for phone number. */
    private static final int MAX_PHONE_LENGTH = 50;
    /** Max length for website URL. */
    private static final int MAX_WEBSITE_LENGTH = 500;
    /** Max length for vicinity string. */
    private static final int MAX_VICINITY_LENGTH = 200;
    /** Max length for place ID. */
    private static final int MAX_PLACE_ID_LENGTH = 100;
    /** Min price level (Google Places). */
    private static final int PRICE_LEVEL_MIN = 1;
    /** Max price level (Google Places). */
    private static final int PRICE_LEVEL_MAX = 4;
    /** Minimum user ratings total (non-negative). */
    private static final int MIN_RATINGS_TOTAL = 0;
    /** Rating scale max. */
    private static final String RATING_MAX = "5.0";
    /** Rating scale min. */
    private static final String RATING_MIN = "0.0";
    /** Allowed location types for Pattern. */
    private static final String LOCATION_TYPE_PATTERN =
            "^(attraction|cafe|hotel|restaurant|transport|park|museum|beach|mountain|"
            + "landmark|shopping|entertainment|religious|hospital|airport|station|viewpoint|"
            + "nature|sports|accommodation)$";

    /** Associates the location with a chat/session. */
    @Field("sessionId")
    @NotBlank
    private String sessionId;

    /** Latitude. */
    @Field("latitude")
    @NotNull(message = "Latitude cannot be null")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90 degrees")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90 degrees")
    private Double latitude;

    /** Longitude. */
    @Field("longitude")
    @NotNull(message = "Longitude cannot be null")
    @DecimalMin(value = "-180.0",
            message = "Longitude must be between -180 and 180 degrees")
    @DecimalMax(value = "180.0",
            message = "Longitude must be between -180 and 180 degrees")
    private Double longitude;

    /** Optional name or description of the location. */
    @Field("name")
    @Size(max = MAX_NAME_LENGTH, message = "Location name cannot exceed 200 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-.,()\\u00C0-\\u017F]*$",
            message = "Location name contains invalid characters")
    private String name;

    /** E.g. attraction, cafe, hotel, park, museum, beach. */
    @Field("type")
    @Size(max = MAX_TYPE_LENGTH, message = "Location type cannot exceed 50 characters")
    @Pattern(regexp = LOCATION_TYPE_PATTERN,
            message = "Location type must be one of the allowed types")
    private String type;

    /** Photo URLs from Google Places API. */
    @Field("photos")
    private java.util.List<String> photos;

    /** Google Places rating (0-5 scale). */
    @Field("rating")
    @DecimalMin(value = RATING_MIN, message = "Rating must be between 0 and 5")
    @DecimalMax(value = RATING_MAX, message = "Rating must be between 0 and 5")
    private Double rating;

    /** Number of reviews. */
    @Field("userRatingsTotal")
    @Min(value = MIN_RATINGS_TOTAL, message = "User ratings total must be non-negative")
    private Integer userRatingsTotal;

    /** Google Places price level (1-4 scale). */
    @Field("priceLevel")
    @Min(value = PRICE_LEVEL_MIN, message = "Price level must be between 1 and 4")
    @Max(value = PRICE_LEVEL_MAX, message = "Price level must be between 1 and 4")
    private Integer priceLevel;

    /** Business phone number. */
    @Field("phoneNumber")
    @Size(max = MAX_PHONE_LENGTH, message = "Phone number cannot exceed 50 characters")
    private String phoneNumber;

    /** Business website URL. */
    @Field("website")
    @Size(max = MAX_WEBSITE_LENGTH, message = "Website URL cannot exceed 500 characters")
    private String website;

    /** Business address or vicinity. */
    @Field("vicinity")
    @Size(max = MAX_VICINITY_LENGTH, message = "Vicinity cannot exceed 200 characters")
    private String vicinity;

    /** Google Places ID for detailed lookups. */
    @Field("placeId")
    @Size(max = MAX_PLACE_ID_LENGTH, message = "Place ID cannot exceed 100 characters")
    private String placeId;

    /** Current open status (if available). */
    @Field("isOpen")
    private Boolean isOpen;

    /** Opening hours text. */
    @Field("weekdayText")
    private java.util.List<String> weekdayText;

    /** Created timestamp. */
    @Field("createdAt")
    private LocalDateTime createdAt;

    /** Last updated timestamp. */
    @Field("updatedAt")
    private LocalDateTime updatedAt;
}
