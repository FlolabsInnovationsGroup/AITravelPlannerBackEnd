package com.springboot.FlomadAIplanner.service.viator;

import com.springboot.FlomadAIplanner.dto.viator.BulkFreeTextSearchRequest;
import com.springboot.FlomadAIplanner.dto.viator.FreeTextSearchRequest;
import com.springboot.FlomadAIplanner.dto.viator.FreeTextSearchResponse;
import com.springboot.FlomadAIplanner.exceptions.api.ViatorAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ViatorAuxiliaryService {

    private final ViatorClient viatorClient;

    public ViatorAuxiliaryService(ViatorClient viatorClient) {
        this.viatorClient = viatorClient;
    }

    public FreeTextSearchResponse searchFreeText(String acceptLang, FreeTextSearchRequest request) throws ViatorAPIException {
        log.info("Searching free text with term: {}", request.getSearchTerm());
        try {
            log.info("Performing free text search with term the following header {}, and parameters: {}", acceptLang, request);
            FreeTextSearchResponse response = viatorClient.searchFreeText("application/json;version=2.0", acceptLang, request);
            log.info("Received search results: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error performing free text search", e);
            throw new ViatorAPIException("Error performing free text search", e);
        }
    }

    public List<FreeTextSearchResponse> bulkSearchFreeText(String acceptLang, BulkFreeTextSearchRequest request) throws ViatorAPIException {
        log.info("Performing bulk text searches for multiple queries.");

        List<FreeTextSearchResponse> results = new ArrayList<>();
        try {
            FreeTextSearchRequest freeTextSearchRequest = buildFreeTextSearchRequest(request);
            for (String searchTerm : request.getSearchTerms()) {
                freeTextSearchRequest.setSearchTerm(searchTerm);
                log.info("Performing free text search with term: {}", freeTextSearchRequest.getSearchTerm());
                FreeTextSearchResponse response = viatorClient.searchFreeText("application/json;version=2.0", acceptLang, freeTextSearchRequest);
                log.info("Received search results: {}", response);
                results.add(response);
            }
            return results;
        } catch (Exception e) {
            log.error("Error performing free text search", e);
            throw new ViatorAPIException("Error performing free text search", e);
        }
    }

    private FreeTextSearchRequest buildFreeTextSearchRequest(BulkFreeTextSearchRequest bulkRequest) {
        return FreeTextSearchRequest.builder()
                .productFiltering(bulkRequest.getProductFiltering())
                .productSorting(bulkRequest.getProductSorting())
                .searchTypes(bulkRequest.getSearchTypes())
                .currency(bulkRequest.getCurrency())
                .build();
    }
}
