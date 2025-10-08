package com.springboot.FlomadAIplanner.service.amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.amadeus.*;
import com.springboot.FlomadAIplanner.exceptions.api.AmadeusAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AmadeusFlightsService {

    private final AmadeusClient amadeusClient;

    public AmadeusFlightsService(AmadeusClient amadeusClient) {
        this.amadeusClient = amadeusClient;
    }

    public FlightOffersPostResponse getFlightOffersPost(FlightOffersPostRequest flightOffersPostRequest) throws AmadeusAPIException {
        log.info("Getting flight offers with POST request: {}", flightOffersPostRequest);
        try {
            FlightOffersPostResponse response = amadeusClient.getFlightOffersPost("GET", flightOffersPostRequest);
            log.info("Received flight offers from Amadeus using POST request. Response length: {}", response.toString().length());
            return response;
        } catch (Exception e) {
            log.error("Error getting flight offers with POST request", e);
            throw new AmadeusAPIException("Error getting flight offers with POST request", e);
        }
    }

    public JsonNode getFlightDestinations(FlightDestinationsRequest request) throws AmadeusAPIException {
        log.info("Getting flight destinations from origin: {} with max price: {}", request.getOrigin(), request.getMaxPrice());
        try {
            JsonNode response = amadeusClient.getFlightDestinations(
                    request.getOrigin(),
                    request.getMaxPrice(),
                    request.getDepartureDate(),
                    request.getOneWay(),
                    request.getDuration(),
                    request.getNonStop(),
                    request.getViewBy()
            );
            log.info("Received response from Amadeus: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error getting flight destinations", e);
            throw new AmadeusAPIException("Error getting flight destinations", e);
        }
    }

    public JsonNode getOfferPricing(FlightOfferPricingRequest request) throws AmadeusAPIException {
        log.info("Verifying flight offer pricing");
        try {
            JsonNode response = amadeusClient.getOfferPricing("GET", request.getOffer());
            log.info("Received response from Amadeus for flight offer pricing");
            return response;
        } catch (Exception e) {
            log.error("Error verifying flight offer pricing", e);
            throw new AmadeusAPIException("Error verifying flight offer pricing", e);
        }
    }

    public JsonNode getAirportCode(FlightsAirportLocationsRequest request) throws AmadeusAPIException {
        log.info("Getting airport code for city: {}", request.getKeyword());
        try {
            JsonNode response = amadeusClient.getLocations(
                    request.getSubType(),
                    request.getKeyword(),
                    request.getCountryCode(),
                    request.getPageLimit(),
                    request.getPageOffset(),
                    request.getSort(),
                    request.getView()
            );
            log.info("Received response from Amadeus: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error getting airport code", e);
            throw new AmadeusAPIException("Error getting airport code", e);
        }
    }
}
