package com.springboot.FlomadAIplanner.service.viator;

import com.springboot.FlomadAIplanner.dto.viator.AttractionProductsRequest;
import com.springboot.FlomadAIplanner.exceptions.ViatorAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ViatorAttractionProductsService {

    private final ViatorClient viatorClient;

    public ViatorAttractionProductsService(ViatorClient viatorClient) {
        this.viatorClient = viatorClient;
    }

    public String getAttractionProducts(String acceptLanguage, AttractionProductsRequest request) throws ViatorAPIException {
        log.info("Getting attraction products for SEO ID: {}", request.getSeoId());

        try {
            log.info("Performing attraction products search with the following header {}, and parameters: {}", acceptLanguage, request);
            String response = viatorClient.getAttractionProducts(acceptLanguage, request);
            log.info("Received attraction products: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error getting attraction products", e);
            throw new ViatorAPIException("Error getting attraction products", e);
        }
    }
}
