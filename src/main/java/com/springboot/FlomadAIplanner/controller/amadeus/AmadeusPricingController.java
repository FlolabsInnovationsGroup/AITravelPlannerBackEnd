package com.springboot.FlomadAIplanner.controller.amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.exceptions.AmadeusAPIException;
import com.springboot.FlomadAIplanner.service.amadeus.AmadeusFlightsService;
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
public class AmadeusPricingController {

    private final AmadeusFlightsService amadeusFlightsService;

    @Autowired
    public AmadeusPricingController(AmadeusFlightsService amadeusFlightsService) {
        this.amadeusFlightsService = amadeusFlightsService;
    }

    @PostMapping("/pricing")
    public ResponseEntity<JsonNode> getOfferPricing(@RequestBody @NotNull @NotEmpty String offer) throws AmadeusAPIException {
        log.info("Received offer: {}", offer);

        JsonNode response = amadeusFlightsService.getOfferPricing(offer);

        log.info("Received response from Amadeus API");
        return ResponseEntity.ok(response);
    }
}
