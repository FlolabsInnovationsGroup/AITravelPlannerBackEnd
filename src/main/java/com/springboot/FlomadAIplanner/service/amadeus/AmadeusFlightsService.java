package com.springboot.FlomadAIplanner.service.amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightOffersRequest;
import com.springboot.FlomadAIplanner.exceptions.AmadeusAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AmadeusFlightsService {

    private final AmadeusClient amadeusClient;

    @Autowired
    public AmadeusFlightsService(AmadeusClient amadeusClient) {
        this.amadeusClient = amadeusClient;
    }

    public JsonNode getFlightOffersPost(FlightOffersRequest flightOffersRequest) throws AmadeusAPIException {
        log.info("Getting flight offers with POST request");
        try {
            JsonNode response = amadeusClient.getFlightOffersPost("GET", flightOffersRequest);
            log.info("Received flight offers from Amadeus using POST request");
            return response;
        } catch (Exception e) {
            log.error("Error getting flight offers with POST request", e);
            throw new AmadeusAPIException("Error getting flight offers with POST request", e);
        }
    }

    public JsonNode getFlightOffers(String originLocationCode, String destinationLocationCode, String departureDate, String returnDate, int adults, int max) throws AmadeusAPIException {
        log.info("Getting flight offers from {} to {} for {} adults", originLocationCode, destinationLocationCode, adults);
        try {
            JsonNode response = amadeusClient.getFlightOffers(originLocationCode, destinationLocationCode, departureDate, returnDate, adults, max);
            log.info("Received flight offers from Amadeus");
            return response;
        } catch (Exception e) {
            log.error("Error getting flight offers", e);
            throw new AmadeusAPIException("Error getting flight offers", e);
        }
    }

    public JsonNode getFlightDestinations(String origin, String maxPrice) throws AmadeusAPIException {
        log.info("Getting flight destinations from origin: {} with max price: {}", origin, maxPrice);
        try {
            JsonNode response = amadeusClient.getFlightDestinations(origin, maxPrice);
            log.info("Received response from Amadeus: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error getting flight destinations", e);
            throw new AmadeusAPIException("Error getting flight destinations", e);
        }
    }

    public JsonNode getOfferPricing(String offer) throws AmadeusAPIException {
        log.info("Verifying flight offer pricing");
        try {
            JsonNode response = amadeusClient.getOfferPricing("GET", offer);
            log.info("Received response from Amadeus for flight offer pricing");
            return response;
        } catch (Exception e) {
            log.error("Error verifying flight offer pricing", e);
            throw new AmadeusAPIException("Error verifying flight offer pricing", e);
        }
    }

    public JsonNode getAirportCode(String cityName) throws AmadeusAPIException {
        log.info("Getting airport code for city: {}", cityName);
        try {
            JsonNode response = amadeusClient.getLocations("AIRPORT", cityName, null, null, null);
            log.info("Received response from Amadeus: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error getting airport code", e);
            throw new AmadeusAPIException("Error getting airport code", e);
        }
    }

}
