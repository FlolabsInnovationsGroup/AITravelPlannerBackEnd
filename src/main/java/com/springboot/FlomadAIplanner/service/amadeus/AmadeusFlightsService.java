package com.springboot.FlomadAIplanner.service.amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightDestinationsRequest;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightOfferPricingRequest;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightOffersPostRequest;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightOffersPostResponse;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightsAirportLocationsRequest;
import com.springboot.FlomadAIplanner.exceptions.api.AmadeusAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public final class AmadeusFlightsService {

    /** Amadeus API client. */
    private final AmadeusClient amadeusClient;

    /**
     * Constructor.
     *
     * @param pAmadeusClient Amadeus API client
     */
    public AmadeusFlightsService(final AmadeusClient pAmadeusClient) {
        this.amadeusClient = pAmadeusClient;
    }

    /**
     * Fetches flight offers via POST.
     *
     * @param flightOffersPostRequest request
     * @return flight offers response
     * @throws AmadeusAPIException on API error
     */
    public FlightOffersPostResponse getFlightOffersPost(
            final FlightOffersPostRequest flightOffersPostRequest)
            throws AmadeusAPIException {
        log.info("Getting flight offers POST: {}", flightOffersPostRequest);
        try {
            FlightOffersPostResponse response = amadeusClient.getFlightOffersPost(
                    "GET", flightOffersPostRequest);
            log.info("Received flight offers from Amadeus, length: {}",
                    response.toString().length());
            return response;
        } catch (Exception e) {
            log.error("Error getting flight offers POST", e);
            throw new AmadeusAPIException(
                    "Error getting flight offers with POST request", e);
        }
    }

    /**
     * Fetches flight destinations from Amadeus.
     *
     * @param request destinations request
     * @return JSON response
     * @throws AmadeusAPIException on API error
     */
    public JsonNode getFlightDestinations(
            final FlightDestinationsRequest request)
            throws AmadeusAPIException {
        log.info("Getting flight destinations origin: {} maxPrice: {}",
                request.getOrigin(), request.getMaxPrice());
        try {
            JsonNode response = amadeusClient.getFlightDestinations(
                    request.getOrigin(),
                    request.getMaxPrice(),
                    request.getDepartureDate(),
                    request.getOneWay(),
                    request.getDuration(),
                    request.getNonStop(),
                    request.getViewBy());
            log.info("Received flight destinations from Amadeus: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error getting flight destinations", e);
            throw new AmadeusAPIException(
                    "Error getting flight destinations", e);
        }
    }

    /**
     * Fetches flight offer pricing.
     *
     * @param request pricing request
     * @return JSON response
     * @throws AmadeusAPIException on API error
     */
    public JsonNode getOfferPricing(final FlightOfferPricingRequest request)
            throws AmadeusAPIException {
        log.info("Verifying flight offer pricing");
        try {
            JsonNode response = amadeusClient.getOfferPricing("GET",
                    request.getOffer());
            log.info("Received Amadeus flight offer pricing");
            return response;
        } catch (Exception e) {
            log.error("Error verifying flight offer pricing", e);
            throw new AmadeusAPIException(
                    "Error verifying flight offer pricing", e);
        }
    }

    /**
     * Fetches airport/location code.
     *
     * @param request locations request
     * @return JSON response
     * @throws AmadeusAPIException on API error
     */
    public JsonNode getAirportCode(final FlightsAirportLocationsRequest request)
            throws AmadeusAPIException {
        log.info("Getting airport code for city: {}", request.getKeyword());
        try {
            JsonNode response = amadeusClient.getLocations(
                    request.getSubType(),
                    request.getKeyword(),
                    request.getCountryCode(),
                    request.getPageLimit(),
                    request.getPageOffset(),
                    request.getSort(),
                    request.getView());
            log.info("Received locations from Amadeus: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error getting airport code", e);
            throw new AmadeusAPIException("Error getting airport code", e);
        }
    }
}
