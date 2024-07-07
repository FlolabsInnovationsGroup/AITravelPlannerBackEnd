package com.springboot.FlomadAIplanner.dto.amadeus;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelSearchByGeocodeRequest {

    @NotNull
    @JsonPropertyDescription("REQUIRED. The latitude of the searched geographical point expressed in geometric degrees.")
    private Double latitude;

    @NotNull
    @JsonPropertyDescription("REQUIRED. The longitude of the searched geographical point expressed in geometric degrees.")
    private Double longitude;

    @Min(1)
    @JsonPropertyDescription("Optional. Maximum distance from the geographical coordinates expressed in defined units. Default is 5 'radiusUnit' measurement units.")
    private Integer radius;

    @Pattern(regexp = "KM|MILE")
    @JsonPropertyDescription("Optional. regexp = \"KM|MILE\". Unit of measurement used to express the radius. Default is 'KM'.")
    private String radiusUnit;

    @JsonPropertyDescription("Optional. Array of hotel chain codes. Each code is a string consisting of 2 capital alphabetic characters.")
    private List<String> chainCodes;

    @JsonPropertyDescription("""
            Optional. List of amenities which should be available at the hotel.
            regexp = 'SWIMMING_POOL|SPA|FITNESS_CENTER|AIR_CONDITIONING|RESTAURANT|PARKING|PETS_ALLOWED|AIRPORT_SHUTTLE|BUSINESS_CENTER|DISABLED_FACILITIES|WIFI|MEETING_ROOMS|NO_KID_ALLOWED|TENNIS|GOLF|KITCHEN|ANIMAL_WATCHING|BABY-SITTING|BEACH|CASINO|JACUZZI|SAUNA|SOLARIUM|MASSAGE|VALET_PARKING|BAR or LOUNGE|KIDS_WELCOME|NO_PORN_FILMS|MINIBAR|TELEVISION|WI-FI_IN_ROOM|ROOM_SERVICE|GUARDED_PARKG|SERV_SPEC_MENU'
            """)
    private List<@Pattern(regexp = "SWIMMING_POOL|SPA|FITNESS_CENTER|AIR_CONDITIONING|RESTAURANT|PARKING|PETS_ALLOWED|AIRPORT_SHUTTLE|BUSINESS_CENTER|DISABLED_FACILITIES|WIFI|MEETING_ROOMS|NO_KID_ALLOWED|TENNIS|GOLF|KITCHEN|ANIMAL_WATCHING|BABY-SITTING|BEACH|CASINO|JACUZZI|SAUNA|SOLARIUM|MASSAGE|VALET_PARKING|BAR or LOUNGE|KIDS_WELCOME|NO_PORN_FILMS|MINIBAR|TELEVISION|WI-FI_IN_ROOM|ROOM_SERVICE|GUARDED_PARKG|SERV_SPEC_MENU")
            String> amenities;

    @JsonPropertyDescription("Optional. Hotel ratings (1-5 stars).")
    private List<@Min(1) @Max(5) Integer> ratings;

    @Pattern(regexp = "BEDBANK|DIRECTCHAIN|ALL")
    @JsonPropertyDescription("Optional. Hotel source with values BEDBANK for aggregators, DIRECTCHAIN for GDS/Distribution and ALL for both. Default is 'ALL'.")
    private String hotelSource;
}
