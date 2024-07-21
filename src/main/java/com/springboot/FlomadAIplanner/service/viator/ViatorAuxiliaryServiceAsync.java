package com.springboot.FlomadAIplanner.service.viator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.viator.BulkFreeTextSearchRequest;
import com.springboot.FlomadAIplanner.dto.viator.FreeTextSearchRequest;
import com.springboot.FlomadAIplanner.exceptions.api.ViatorAPIException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViatorAuxiliaryServiceAsync {

    private final ViatorClient viatorClient;

    /**
     * Asynchronously performs a free text search using the provided request.
     *
     * @param acceptLang the language to be used for the search
     * @param request    the search request containing the search term and other parameters
     * @return a CompletableFuture containing the JSON response from the search
     */
    @Async
    protected CompletableFuture<JsonNode> searchFreeTextAsync(String acceptLang, FreeTextSearchRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Performing async free text search: {}", request);
                JsonNode response = viatorClient.searchFreeText("application/json;version=2.0", acceptLang, request);
                log.info("Received async search result for term: {}", request.getSearchTerm());
                return response;
            } catch (Exception e) {
                log.error("Error during async free text search for term: {}, error: {}", request.getSearchTerm(), e.getMessage(), e);
                throw new RuntimeException("Error performing async free text search", e);
            }
        });
    }

    /**
     * Performs bulk text searches for multiple queries and aggregates the results.
     *
     * @param acceptLang  the language to be used for the searches
     * @param bulkRequest the bulk search request containing multiple search terms
     * @return a JSON array node containing the aggregated search results
     * @throws ViatorAPIException if there is an error during the bulk search process
     */
    public JsonNode bulkSearchFreeText(String acceptLang, BulkFreeTextSearchRequest bulkRequest) throws ViatorAPIException {
        log.info("Performing bulk text searches for multiple queries.");

        // Initiate asynchronous searches
        List<CompletableFuture<JsonNode>> futures = initiateAsyncSearches(acceptLang, bulkRequest);

        // Collect results from the asynchronous searches
        return collectSearchResults(futures);
    }

    /**
     * Initiates asynchronous free text searches for each search term in the bulk request.
     *
     * @param acceptLang  the language to be used for the searches
     * @param bulkRequest the bulk search request containing multiple search terms
     * @return a list of CompletableFuture objects, each representing an asynchronous search result
     */
    private List<CompletableFuture<JsonNode>> initiateAsyncSearches(String acceptLang, BulkFreeTextSearchRequest bulkRequest) {
        return bulkRequest.getSearchTerms().parallelStream()
                .map(searchTerm -> {
                    log.info("Performing async free text search with term: {}", searchTerm);
                    FreeTextSearchRequest searchRequest = buildFreeTextSearchRequest(bulkRequest, searchTerm);
                    return searchFreeTextAsync(acceptLang, searchRequest);
                })
                .collect(Collectors.toList());
    }

    /**
     * Collects the results from the asynchronous search futures and aggregates them into a JSON array node.
     *
     * @param futures a list of CompletableFuture objects, each representing an asynchronous search result
     * @return a JSON array node containing the aggregated search results
     */
    private ArrayNode collectSearchResults(List<CompletableFuture<JsonNode>> futures) {
        ArrayNode results = JsonNodeFactory.instance.arrayNode();

        // Wait for all async operations to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Collect results from each future
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

    /**
     * Builds a FreeTextSearchRequest object based on the bulk request and a specific search term.
     *
     * @param original   the original bulk search request
     * @param searchTerm the specific search term for which to build the request
     * @return a FreeTextSearchRequest object containing the search parameters
     */
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
