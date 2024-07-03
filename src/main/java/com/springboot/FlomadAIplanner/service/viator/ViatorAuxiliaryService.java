package com.springboot.FlomadAIplanner.service.viator;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.viator.FreeTextSearchRequest;
import com.springboot.FlomadAIplanner.exceptions.ViatorAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ViatorAuxiliaryService {

    private final ViatorClient viatorClient;

    @Autowired
    public ViatorAuxiliaryService(ViatorClient viatorClient) {
        this.viatorClient = viatorClient;
    }

    public JsonNode searchFreeText(String acceptLang, FreeTextSearchRequest request) throws ViatorAPIException {
        log.info("Searching free text with term: {}", request.getSearchTerm());
        try {
            log.info("Performing free text search with term the following header {}, and parameters: {}", acceptLang, request);
            JsonNode response = viatorClient.searchFreeText("application/json;version=2.0", acceptLang, request);
            log.info("Received search results: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error performing free text search", e);
            throw new ViatorAPIException("Error performing free text search", e);
        }
    }
}
