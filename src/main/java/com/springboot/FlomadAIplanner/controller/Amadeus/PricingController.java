package com.springboot.FlomadAIplanner.controller.Amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.service.Amadeus.AmadeusShoppingService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/amadeus")
public class PricingController {

    private final AmadeusShoppingService amadeusShoppingService;

    @Autowired
    public PricingController(AmadeusShoppingService amadeusShoppingService) {
        this.amadeusShoppingService = amadeusShoppingService;
    }

    @PostMapping("/pricing")
    public ResponseEntity<JsonNode> getOfferPricing(@RequestBody @NotNull @NotEmpty String offer) {
        log.info("Received offer: {}", offer);

        try {
            JsonNode response = amadeusShoppingService.getOfferPricing(offer);
            if (response == null) {
                log.error("Failed to verify flight offer pricing");
                return ResponseEntity.status(500).body(null);
            }
            log.info("Received response from Amadeus API");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing the request: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
}
