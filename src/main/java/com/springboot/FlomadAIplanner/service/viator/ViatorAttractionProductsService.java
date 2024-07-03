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

    public String getAttractionProducts(String acceptLanguage, int seoId, String topX, String sortOrder, String currencyCode) throws ViatorAPIException {
        log.info("Getting attraction products for SEO ID: {}", seoId);

        try {
            AttractionProductsRequest request = new AttractionProductsRequest(seoId, topX, sortOrder, currencyCode);
            String response = viatorClient.getAttractionProducts(acceptLanguage, request);
            log.info("Received attraction products: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error getting attraction products", e);
            throw new ViatorAPIException("Error getting attraction products", e);
        }
    }
}
