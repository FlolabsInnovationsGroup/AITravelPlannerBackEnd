package com.springboot.FlomadAIplanner.service.viator;

import com.springboot.FlomadAIplanner.dto.viator.AttractionsRequest;
import com.springboot.FlomadAIplanner.exceptions.ViatorAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ViatorAttractionsService {

    private final ViatorClient viatorClient;

    @Autowired
    public ViatorAttractionsService(ViatorClient viatorClient) {
        this.viatorClient = viatorClient;
    }

    public String getAttractions(String acceptLang, AttractionsRequest request) throws ViatorAPIException {
        log.info("Getting attractions for destination: {}", request.getDestId());
        try {
            log.info("Performing attractions search with the following header {}, and parameters: {}", acceptLang, request);
            String response = viatorClient.getAttractions(acceptLang, request);
            log.info("Received attractions: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error getting attractions", e);
            throw new ViatorAPIException("Error getting attractions", e);
        }
    }
}
