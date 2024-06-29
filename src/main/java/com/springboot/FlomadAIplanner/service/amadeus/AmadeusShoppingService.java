package com.springboot.FlomadAIplanner.service.amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.exceptions.AmadeusAPIException;
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

        try {
            String authorizationHeader = "Bearer " + accessToken;
            JsonNode response = amadeusClient.getFlightDestinations(authorizationHeader, origin, maxPrice);
            log.info("Received response from Amadeus: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error getting flight destinations", e);
            throw new AmadeusAPIException("Error getting flight destinations", e);
        }
    }

    public JsonNode getFlightOffers(String originLocationCode, String destinationLocationCode, String departureDate, String returnDate, int adults, int max) {
        log.info("Getting flight offers from {} to {} for {} adults", originLocationCode, destinationLocationCode, adults);
        String accessToken = amadeusAuthService.getAccessToken();

        try {
            String authorizationHeader = "Bearer " + accessToken;
            JsonNode response = amadeusClient.getFlightOffers(authorizationHeader, originLocationCode, destinationLocationCode, departureDate, returnDate, adults, max);
            log.info("Received flight offers from Amadeus");
            return response;
        } catch (Exception e) {
            log.error("Error getting flight offers", e);
            throw new AmadeusAPIException("Error getting flight offers", e);
        }
    }

    public JsonNode getOfferPricing(String offer) {
        log.info("Verifying flight offer pricing");
        String accessToken = amadeusAuthService.getAccessToken();

        try {
            String authorizationHeader = "Bearer " + accessToken;
            JsonNode response = amadeusClient.getOfferPricing(authorizationHeader, "GET", offer);
            log.info("Received response from Amadeus for flight offer pricing");
            return response;
        } catch (Exception e) {
            log.error("Error verifying flight offer pricing", e);
            throw new AmadeusAPIException("Error verifying flight offer pricing", e);
        }
    }

}
