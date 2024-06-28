package com.springboot.FlomadAIplanner.service.Amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AmadeusShoppingService {

    private final AmadeusAuthService amadeusAuthService;
    private final AmadeusClient amadeusClient;

    @Autowired
    public AmadeusShoppingService(AmadeusAuthService amadeusAuthService, AmadeusClient amadeusClient) {
        this.amadeusAuthService = amadeusAuthService;
        this.amadeusClient = amadeusClient;
    }

    public JsonNode getFlightDestinations(String origin, String maxPrice) {
        log.info("Getting flight destinations from origin: {} with max price: {}", origin, maxPrice);
        String accessToken = amadeusAuthService.getAccessToken();
        if (accessToken == null) {
            log.error("Cannot get flight destinations without access token");
            return null;
        }
        String authorizationHeader = "Bearer " + accessToken;
        JsonNode response = amadeusClient.getFlightDestinations(authorizationHeader, origin, maxPrice);
        log.info("Received response from Amadeus: {}", response);
        return response;
    }

    public JsonNode getFlightOffers(String originLocationCode, String destinationLocationCode, String departureDate, String returnDate, int adults, int max) {
        log.info("Getting flight offers from {} to {} for {} adults", originLocationCode, destinationLocationCode, adults);
        String accessToken = amadeusAuthService.getAccessToken();
        if (accessToken == null) {
            log.error("Cannot get flight offers without access token");
            return null;
        }
        String authorizationHeader = "Bearer " + accessToken;
        JsonNode response = amadeusClient.getFlightOffers(authorizationHeader, originLocationCode, destinationLocationCode, departureDate, returnDate, adults, max);
        log.info("Received flight offers from Amadeus");
        return response;
    }

    public JsonNode getOfferPricing(String offer) {
        log.info("Verifying flight offer pricing");
        String accessToken = amadeusAuthService.getAccessToken();
        if (accessToken == null) {
            log.error("Cannot verify flight offer pricing without access token");
            return null;
        }
        String authorizationHeader = "Bearer " + accessToken;
        JsonNode response = amadeusClient.getOfferPricing(authorizationHeader, "GET", offer);
        log.info("Received response from Amadeus for flight offer pricing");
        return response;
    }

}
