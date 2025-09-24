package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.dto.viator.BulkFreeTextSearchRequest;
import com.springboot.FlomadAIplanner.dto.viator.FreeTextSearchResponse;
import com.springboot.FlomadAIplanner.exceptions.api.ViatorAPIException;
import com.springboot.FlomadAIplanner.service.viator.ViatorAuxiliaryServiceAsync;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
@Description("""
        Searches for tours, activities, and experiences using natural language search terms. This function finds tours, 
        attractions, and experiences across multiple destinations using flexible text-based queries.
        
        OPTIMIZED FOR: Multi-destination searches - process all required destinations in a single function call
        
        Capabilities:
        - Free-text search across destinations (countries, cities, landmarks)
        - Finds tours, attractions, products, and unique experiences
        - Viator platform integration for comprehensive activity options
        - Supports multiple search terms simultaneously for efficiency
        
        USAGE STRATEGY: Search for tours in ALL required places in ONE call rather than separate calls per destination.
        Minimum: 1 search term, Maximum: unlimited search terms per call
        
        Examples: "Paris Eiffel Tower tours", "Tokyo food experiences", "Adventure activities Bali", "Historical tours Rome"
        
        Perfect for comprehensive itinerary planning when travelers need activities across multiple destinations.
        
        REQUIRED: 'request' parameter must be provided
        DESTINATION: Can be country, city, town, region
        ATTRACTION: Famous places, monuments, museums, landmarks
        PRODUCT: Tours, activities, experiences with purchase links and media
        
        Use CurrentDateTimeFunction before this function to get current date and time for accurate availability.
        """)
public class ToursFreeTextSearchFunction implements Function<ToursFreeTextSearchFunction.FreeTextSearchRequestWrapper, List<FreeTextSearchResponse>> {

    private final ViatorAuxiliaryServiceAsync viatorAuxiliaryService;

    @Autowired
    public ToursFreeTextSearchFunction(ViatorAuxiliaryServiceAsync viatorAuxiliaryService) {
        this.viatorAuxiliaryService = viatorAuxiliaryService;
    }

    @Override
    @Valid
    public List<FreeTextSearchResponse> apply(FreeTextSearchRequestWrapper requestWrapper) {
        log.info("Performing free text search with term: {}", requestWrapper.getRequest().getSearchTerms());
        try {
            List<FreeTextSearchResponse> response = viatorAuxiliaryService.bulkSearchFreeText(requestWrapper.getAcceptLanguage(), requestWrapper.getRequest());
            log.info("Received free text search response from Viator. Response length: {}", response.toString().length());
            return response;
        } catch (ViatorAPIException e) {
            log.error("Error performing free text search", e);
            return Collections.singletonList(FreeTextSearchResponse.builder().errorMessage(e.getMessage()).build());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonClassDescription("Wrapper for the free text search request. JSON Schema of type: 'object'")
    public static class FreeTextSearchRequestWrapper {

        @NotBlank
        @JsonPropertyDescription("REQUIRED. Can't be null. Specifies the language into which the natural-language fields in the response will be translated if needed. Example: en-US. By default use en-US")
        private String acceptLanguage = "en-US";

        @Valid
        @NotNull
        @JsonPropertyDescription("REQUIRED. Can't be null. The free text search request containing the search term and filters for the search.")
        private BulkFreeTextSearchRequest request;
    }

}
