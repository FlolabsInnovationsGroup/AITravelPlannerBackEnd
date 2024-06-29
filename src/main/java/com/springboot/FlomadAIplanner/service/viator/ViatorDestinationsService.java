package com.springboot.FlomadAIplanner.service.viator;

import com.springboot.FlomadAIplanner.exceptions.ViatorAPIException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViatorDestinationsService {

    private final ViatorClient viatorClient;

    public String getDestinations() {
        log.info("Getting destinations");
        try {
            String response = viatorClient.getDestinations();
            log.info("Received destinations: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error getting destinations", e);
            throw new ViatorAPIException("Error getting destinations", e);
        }
    }
}
