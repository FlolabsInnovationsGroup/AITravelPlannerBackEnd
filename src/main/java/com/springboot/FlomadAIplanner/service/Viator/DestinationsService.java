package com.springboot.FlomadAIplanner.service.Viator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DestinationsService {

    private final ViatorClient viatorClient;

    @Autowired
    public DestinationsService(ViatorClient viatorClient) {
        this.viatorClient = viatorClient;
    }

    public String getDestinations() {
        log.info("Getting destinations");
        String response = viatorClient.getDestinations();
        log.info("Received destinations: {}", response);
        return response;
    }
}
