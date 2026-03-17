package com.springboot.FlomadAIplanner.service.viator;

import com.springboot.FlomadAIplanner.dto.viator.BulkFreeTextSearchRequest;
import com.springboot.FlomadAIplanner.dto.viator.FreeTextSearchRequest;
import com.springboot.FlomadAIplanner.dto.viator.FreeTextSearchResponse;
import com.springboot.FlomadAIplanner.exceptions.api.ViatorAPIException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViatorAuxiliaryServiceAsync {

    /** Viator API client. */
    private final ViatorClient viatorClient;

    /**
     * Asynchronously performs a free text search.
     *
     * @param acceptLang language for search
     * @param request search request
     * @return future with search response
     */
    @Async
    protected CompletableFuture<FreeTextSearchResponse> searchFreeTextAsync(
            final String acceptLang,
            final FreeTextSearchRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Async free text search term: {}",
                        request.getSearchTerm());
                FreeTextSearchResponse response = viatorClient.searchFreeText(
                        "application/json;version=2.0", acceptLang, request);
                log.info("Async search result length: {}, term: {}",
                        response.toString().length(),
                        request.getSearchTerm());
                return response;
            } catch (Exception e) {
                log.error("Async free text search failed term: {}: {}",
                        request.getSearchTerm(), e.getMessage(), e);
                throw new RuntimeException(
                        "Error performing async free text search", e);
            }
        });
    }

    /**
     * Bulk free text search; aggregates results.
     *
     * @param acceptLang language
     * @param bulkRequest bulk request
     * @return list of search responses
     * @throws ViatorAPIException on API error
     */
    public List<FreeTextSearchResponse> bulkSearchFreeText(
            final String acceptLang,
            final BulkFreeTextSearchRequest bulkRequest)
            throws ViatorAPIException {
        log.info("Performing bulk text searches for multiple queries.");
        List<CompletableFuture<FreeTextSearchResponse>> futures =
                initiateAsyncSearches(acceptLang, bulkRequest);
        return collectSearchResults(futures);
    }

    private List<CompletableFuture<FreeTextSearchResponse>>
            initiateAsyncSearches(
            final String acceptLang,
            final BulkFreeTextSearchRequest bulkRequest) {
        return bulkRequest.getSearchTerms().parallelStream()
                .map(searchTerm -> {
                    log.info("Async free text search term: {}", searchTerm);
                    FreeTextSearchRequest searchRequest =
                            buildFreeTextSearchRequest(bulkRequest, searchTerm);
                    return searchFreeTextAsync(acceptLang, searchRequest);
                })
                .collect(Collectors.toList());
    }

    private List<FreeTextSearchResponse> collectSearchResults(
            final List<CompletableFuture<FreeTextSearchResponse>> futures) {
        List<FreeTextSearchResponse> results = new ArrayList<>();
        CompletableFuture.allOf(
                futures.toArray(new CompletableFuture<?>[0])).join();
        futures.forEach(f -> {
            try {
                results.add(f.join());
            } catch (Exception e) {
                log.error("Failed to get future result", e);
                results.add(FreeTextSearchResponse.builder()
                        .errorMessage(e.getMessage()).build());
            }
        });
        return results;
    }

    private FreeTextSearchRequest buildFreeTextSearchRequest(
            final BulkFreeTextSearchRequest original,
            final String searchTerm) {
        return FreeTextSearchRequest.builder()
                .productFiltering(original.getProductFiltering())
                .productSorting(original.getProductSorting())
                .searchTypes(original.getSearchTypes())
                .currency(original.getCurrency())
                .searchTerm(searchTerm)
                .build();
    }
}
