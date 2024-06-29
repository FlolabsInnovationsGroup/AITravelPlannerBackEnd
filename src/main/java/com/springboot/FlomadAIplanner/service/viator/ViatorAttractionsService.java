package com.springboot.FlomadAIplanner.service.viator;

import com.springboot.FlomadAIplanner.DTO.AttractionsRequest;
import com.springboot.FlomadAIplanner.exceptions.ViatorAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ViatorAttractionsService {

    private final static String ACCEPT_LANGUAGE = "en-US";

    private final ViatorClient viatorClient;

    @Autowired
    public ViatorAttractionsService(ViatorClient viatorClient) {
        this.viatorClient = viatorClient;
    }

    public String getAttractions(int destId, String topX, String sortOrder) {
        log.info("Getting attractions for destination: {}", destId);
        try {
            AttractionsRequest request = new AttractionsRequest(destId, topX, sortOrder);
            String response = viatorClient.getAttractions(ACCEPT_LANGUAGE, request);
            log.info("Received attractions: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error getting attractions", e);
            throw new ViatorAPIException("Error getting attractions", e);
        }
    }
}
