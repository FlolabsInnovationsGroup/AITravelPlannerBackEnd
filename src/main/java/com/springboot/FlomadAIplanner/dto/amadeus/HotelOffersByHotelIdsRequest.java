package com.springboot.FlomadAIplanner.dto.amadeus;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelOffersByHotelIdsRequest {

    @NotEmpty
    @JsonPropertyDescription("REQUIRED. Parameter type is List. Amadeus property codes on 8 chars. Example: ['MCLONGHM', 'UIPARPAS']. Ensure this converts to multiple `hotelId` entries")
    private List<String> hotelIds;

    @Min(1)
    @Max(9)
    @JsonPropertyDescription("Number of adult guests (1-9) per room. Default value: 1.")
    private Integer adults;

    @NotBlank
    @JsonPropertyDescription("Check-in date of the stay (hotel local date). Format YYYY-MM-DD. The lowest accepted value is today's date.")
    private String checkInDate;

    @NotBlank
    @JsonPropertyDescription("Check-out date of the stay (hotel local date). Format YYYY-MM-DD. Must be at least one day after checkInDate.")
    private String checkOutDate;

    @Pattern(regexp = "[A-Z]{2}")
    @JsonPropertyDescription("regexp = \"[A-Z]{2}\". Code of the country of residence of the traveler expressed using ISO 3166-1 format. If needed Currency Functions could be used to obtain this value.")
    private String countryOfResidence;

    @Min(1)
    @Max(9)
    @JsonPropertyDescription("Number of rooms requested (1-9). Default value: 1.")
    private Integer roomQuantity;

    @JsonPropertyDescription("Filter hotel offers by price per night interval (ex: 200-300). Include currency in the format.")
    private String priceRange;

    @NotBlank
    @JsonPropertyDescription("Currency code in ISO format to specify pricing information.")
    private String currency;

    @Pattern(regexp = "GUARANTEE|DEPOSIT|NONE")
    @JsonPropertyDescription("regexp = \"GUARANTEE|DEPOSIT|NONE\". Filter the response based on a specific payment type. Default: 'NONE'.")
    private String paymentPolicy;

    @Pattern(regexp = "ROOM_ONLY|BREAKFAST|HALF_BOARD|FULL_BOARD|ALL_INCLUSIVE")
    @JsonPropertyDescription("regexp = \"ROOM_ONLY|BREAKFAST|HALF_BOARD|FULL_BOARD|ALL_INCLUSIVE\". Filter response based on available meal options.")
    private String boardType;

    @JsonPropertyDescription("Include sold out properties in the results. Default: false.")
    private Boolean includeClosed = true;

    @JsonPropertyDescription("Return only the cheapest offer per hotel. Default: true.")
    private Boolean bestRateOnly;

    @Pattern(regexp = "^[a-zA-Z]{2}(-[a-zA-Z]{2})?$")
    @JsonPropertyDescription("Requested language of descriptive texts. Example: 'en', 'fr-FR'.")
    private String lang;
}
