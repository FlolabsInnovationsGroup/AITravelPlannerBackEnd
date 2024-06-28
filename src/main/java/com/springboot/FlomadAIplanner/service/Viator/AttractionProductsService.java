package com.springboot.FlomadAIplanner.service.Viator;

import com.springboot.FlomadAIplanner.DTO.AttractionProductsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AttractionProductsService {

    private final ViatorClient viatorClient;

    public AttractionProductsService(ViatorClient viatorClient) {
        this.viatorClient = viatorClient;
    }

    public String getAttractionProducts(String acceptLanguage, int seoId, String topX, String sortOrder, String currencyCode) {
        log.info("Getting attraction products for SEO ID: {}", seoId);

        AttractionProductsRequest request = new AttractionProductsRequest(seoId, topX, sortOrder, currencyCode);

        String response = viatorClient.getAttractionProducts(acceptLanguage, request);
        log.info("Received attraction products: {}", response);

        return response;
    }
}
