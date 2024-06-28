package com.springboot.FlomadAIplanner.controller.Amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.service.Amadeus.AmadeusShoppingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/amadeus")
public class FlightOfferController {

    private final AmadeusShoppingService amadeusShoppingService;

    @Autowired
    public FlightOfferController(AmadeusShoppingService amadeusShoppingService) {
        this.amadeusShoppingService = amadeusShoppingService;
    }

    @GetMapping("/flight-offers")
    public ResponseEntity<String> getFlightOffers(@RequestParam String originLocationCode,
                                                  @RequestParam String destinationLocationCode,
                                                  @RequestParam String departureDate,
                                                  @RequestParam String returnDate,
                                                  @RequestParam int adults,
                                                  @RequestParam int max) {
        log.info("Retrieving flight offers from {} to {} for {} adults", originLocationCode, destinationLocationCode, adults);

        try {
            // Use the service to get flight offers
            JsonNode response = amadeusShoppingService.getFlightOffers(originLocationCode, destinationLocationCode, departureDate, returnDate, adults, max);
            if (response == null) {
                log.error("Failed to retrieve flight offers");
                return ResponseEntity.status(500).body("Failed to retrieve flight offers");
            }
            log.info("Received flight offers from Amadeus API");
            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            log.error("Error retrieving flight offers from Amadeus API", e);
            return ResponseEntity.status(500).body("Error retrieving flight offers from Amadeus API");
        }
    }
}
