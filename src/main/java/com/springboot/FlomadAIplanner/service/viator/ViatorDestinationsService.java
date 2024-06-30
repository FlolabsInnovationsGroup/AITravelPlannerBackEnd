package com.springboot.FlomadAIplanner.service.viator;

import com.springboot.FlomadAIplanner.exceptions.ViatorAPIException;
import com.springboot.FlomadAIplanner.service.amadeus.AmadeusClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViatorDestinationsService {

    private final ViatorClient viatorClient;
    private final AmadeusClient amadeusClient;

    public String getDestinations() throws ViatorAPIException {
        log.info("Getting destinations");
        try {
            amadeusClient.getOfferPricing("LON", "2021-12-01", "2021-12-02");

            String response = viatorClient.getDestinations();
            log.info("Received destinations response.");
            return response;
        } catch (Exception e) {
            log.error("Error getting destinations", e);
            throw new ViatorAPIException("Error getting destinations", e);
        }
    }
}
