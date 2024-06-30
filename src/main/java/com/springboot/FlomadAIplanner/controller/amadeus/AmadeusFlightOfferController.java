package com.springboot.FlomadAIplanner.controller.amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.exceptions.AmadeusAPIException;
import com.springboot.FlomadAIplanner.service.amadeus.AmadeusShoppingService;
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
public class AmadeusFlightOfferController {

    private final AmadeusShoppingService amadeusShoppingService;

    @Autowired
    public AmadeusFlightOfferController(AmadeusShoppingService amadeusShoppingService) {
        this.amadeusShoppingService = amadeusShoppingService;
    }

    @GetMapping("/flight-offers")
    public ResponseEntity<String> getFlightOffers(@RequestParam String originLocationCode,
                                                  @RequestParam String destinationLocationCode,
                                                  @RequestParam String departureDate,
                                                  @RequestParam String returnDate,
                                                  @RequestParam int adults,
                                                  @RequestParam int max) throws AmadeusAPIException {
        log.info("Retrieving flight offers from {} to {} for {} adults", originLocationCode, destinationLocationCode, adults);

        // Use the service to get flight offers
        JsonNode response = amadeusShoppingService.getFlightOffers(originLocationCode, destinationLocationCode, departureDate, returnDate, adults, max);
        if (response == null) {
            log.error("Failed to retrieve flight offers");
            return ResponseEntity.status(500).body("Failed to retrieve flight offers");
        }
        log.info("Received flight offers from Amadeus API");
        return ResponseEntity.ok(response.toString());
    }
}
