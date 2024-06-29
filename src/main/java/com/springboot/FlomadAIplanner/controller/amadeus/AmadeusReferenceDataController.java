package com.springboot.FlomadAIplanner.controller.amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.service.amadeus.ReferenceDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @class
 * @description Amadeus API only accepts IATA code rather than city name, so translation is needed here
 */
@Slf4j
@RestController
@RequestMapping("/api/amadeus")
public class AmadeusReferenceDataController {

    private final ReferenceDataService referenceDataService;

    public AmadeusReferenceDataController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping("/reference_data")
    public JsonNode getAirportCode(@RequestParam String cityName) {
        log.info("Retrieving airport code for city: {}", cityName);
        return referenceDataService.getAirportCode(cityName);
    }
}

