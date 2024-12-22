package com.springboot.FlomadAIplanner.dto.amadeus;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightOfferPricingRequest {

    @NotBlank
    @JsonPropertyDescription("""
            Required. List of criteria to confirm the price of a flight offer.
            Must be in the following view: {
                                             "data": {
                                               "type": "flight-offers-pricing",
                                               "flightOffers": [] // List of flight offers to confirm the price received from the flight offers search in Json format.
                                             }
                                             }\s
            """)
    private String offer;
}
