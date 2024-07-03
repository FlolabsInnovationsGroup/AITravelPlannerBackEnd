package com.springboot.FlomadAIplanner.service.amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.exceptions.AmadeusAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReferenceDataService {

    private final AmadeusAuthService amadeusAuthService;
    private final AmadeusClient amadeusClient;

    @Autowired
    public ReferenceDataService(AmadeusAuthService amadeusAuthService, AmadeusClient amadeusClient) {
        this.amadeusAuthService = amadeusAuthService;
        this.amadeusClient = amadeusClient;
    }

    public JsonNode getAirportCode(String cityName) throws AmadeusAPIException {
        log.info("Getting airport code for city: {}", cityName);
        String accessToken = amadeusAuthService.getAccessToken();

        try {
            String authorizationHeader = "Bearer " + accessToken;
            JsonNode response = amadeusClient.getLocations(authorizationHeader, "AIRPORT", cityName, null, null, null);
            log.info("Received response from Amadeus: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error getting airport code", e);
            throw new AmadeusAPIException("Error getting airport code", e);
        }
    }
}
