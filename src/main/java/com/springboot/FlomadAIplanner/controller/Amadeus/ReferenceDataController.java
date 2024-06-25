package com.springboot.FlomadAIplanner.controller.Amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.service.Amadeus.ReferenceDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

////////////////////////////////////////////

/**
 * @class
 * @description Amadeus API only accepts IATA code rather than city name, so translation is needed here
 */
//////////////////////////////////////////
@Slf4j
@RestController
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    public ReferenceDataController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping("/reference_data")
    public JsonNode getAirportCode(@RequestParam String cityName) {
        log.info("Retrieving airport code for city: {}", cityName);
        return referenceDataService.getAirportCode(cityName);
    }
}

