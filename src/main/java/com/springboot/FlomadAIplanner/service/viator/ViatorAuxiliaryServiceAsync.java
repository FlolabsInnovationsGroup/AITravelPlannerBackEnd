package com.springboot.FlomadAIplanner.service.viator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.viator.BulkFreeTextSearchRequest;
import com.springboot.FlomadAIplanner.dto.viator.FreeTextSearchRequest;
import com.springboot.FlomadAIplanner.exceptions.ViatorAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class ViatorAuxiliaryServiceAsync {

    private final ViatorClient viatorClient;

    @Autowired
    public ViatorAuxiliaryServiceAsync(ViatorClient viatorClient) {
        this.viatorClient = viatorClient;
    }

    @Async
    protected CompletableFuture<JsonNode> searchFreeTextAsync(String acceptLang, FreeTextSearchRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Performing async free text search with term: {}", request.getSearchTerm());
                JsonNode response = viatorClient.searchFreeText("application/json;version=2.0", acceptLang, request);
                log.info("Received async search result for term: {}", request.getSearchTerm());
                return response;
            } catch (Exception e) {
                log.error("Error during async free text search for term: {}, error: {}", request.getSearchTerm(), e.getMessage(), e);
                throw new RuntimeException("Error performing async free text search", e);
            }
        });
    }

    public JsonNode bulkSearchFreeText(String acceptLang, BulkFreeTextSearchRequest bulkRequest) throws ViatorAPIException {
        log.info("Performing bulk text searches for multiple queries.");

        List<CompletableFuture<JsonNode>> futures = bulkRequest.getSearchTerms().stream()
                .map(searchTerm -> {
                    log.info("Performing async free text search with term: {}", searchTerm);
                    FreeTextSearchRequest searchRequest = buildFreeTextSearchRequest(bulkRequest, searchTerm);
                    return searchFreeTextAsync(acceptLang, searchRequest);
                })
                .toList();

        ArrayNode results = JsonNodeFactory.instance.arrayNode();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join(); // Wait for all async operations to complete

        futures.forEach(f -> {
            try {
                results.add(f.join());
            } catch (Exception e) {
                log.error("Failed to get future result", e);
                results.add(JsonNodeFactory.instance.objectNode().put("error", e.getMessage()));
            }
        });

        return results;
    }

    private FreeTextSearchRequest buildFreeTextSearchRequest(BulkFreeTextSearchRequest original, String searchTerm) {
        return FreeTextSearchRequest.builder()
                .productFiltering(original.getProductFiltering())
                .productSorting(original.getProductSorting())
                .searchTypes(original.getSearchTypes())
                .currency(original.getCurrency())
                .searchTerm(searchTerm)
                .build();
    }
}
