package com.springboot.FlomadAIplanner.controller.amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightOfferPricingRequest;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightOffersPostRequest;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightsAirportLocationsRequest;
import com.springboot.FlomadAIplanner.exceptions.api.AmadeusAPIException;
import com.springboot.FlomadAIplanner.service.amadeus.AmadeusFlightsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/amadeus")
@Validated
public class AmadeusFlightOfferController {

    private final AmadeusFlightsService amadeusFlightsService;

    @Autowired
    public AmadeusFlightOfferController(AmadeusFlightsService amadeusFlightsService) {
        this.amadeusFlightsService = amadeusFlightsService;
    }

    @PostMapping("/flight-offers")
    public ResponseEntity<String> getFlightOffersPost(@Valid @RequestBody FlightOffersPostRequest flightOffersPostRequest) throws AmadeusAPIException {
        log.info("Retrieving flight offers with POST request");

        // Use the service to get flight offers
        JsonNode response = amadeusFlightsService.getFlightOffersPost(flightOffersPostRequest);
        if (response == null) {
            log.error("Failed to retrieve flight offers with POST request");
            return ResponseEntity.status(500).body("Failed to retrieve flight offers");
        }
        log.info("Received flight offers from Amadeus API using POST request");
        return ResponseEntity.ok(response.toString());
    }

    @PostMapping("/pricing")
    public ResponseEntity<JsonNode> getOfferPricing(@RequestBody @NotNull @NotEmpty FlightOfferPricingRequest offer) throws AmadeusAPIException {
        log.info("Received offer: {}", offer);

        JsonNode response = amadeusFlightsService.getOfferPricing(offer);

        log.info("Received response from Amadeus API");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/airport-location")
    public ResponseEntity<JsonNode> getAirportLocation(@RequestBody FlightsAirportLocationsRequest locationsRequest) throws AmadeusAPIException {
        log.info("Received locations request: {}", locationsRequest);

        JsonNode response = amadeusFlightsService.getAirportCode(locationsRequest);

        log.info("Received response from Amadeus API");
        return ResponseEntity.ok(response);
    }
}
