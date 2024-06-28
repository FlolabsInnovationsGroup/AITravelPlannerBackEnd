package com.springboot.FlomadAIplanner.service.Amadeus;

import com.fasterxml.jackson.databind.JsonNode;
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

    public JsonNode getAirportCode(String cityName) {
        log.info("Getting airport code for city: {}", cityName);
        String accessToken = amadeusAuthService.getAccessToken();
        if (accessToken == null) {
            log.error("Cannot get airport code without access token");
            return null;
        }
        String authorizationHeader = "Bearer " + accessToken;
        JsonNode response = amadeusClient.getLocations(authorizationHeader, "CITY,AIRPORT", cityName, null, null, null);
        log.info("Received response from Amadeus: {}", response);
        return response;
    }
}
