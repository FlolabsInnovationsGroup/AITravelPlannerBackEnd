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
        
        Examples: ["Paris Eiffel Tower tours"], ["Tokyo food experiences"], ["Adventure activities Bali"], ["Historical tours Rome"]
        
        CRITICAL: searchTerms must be single-element array with combined location+activity term.
        RIGHT: ["Paris romantic tours"] - WRONG: ["romantic tours", "Paris"]
        
        AUTOMATIC MAP INTEGRATION: This function automatically extracts locations mentioned in tour results 
        and adds them to the user's map for immediate visualization. No additional function calls needed.
        
        Perfect for comprehensive itinerary planning when travelers need activities across multiple destinations.
        
        REQUIRED: 'request' parameter must be provided
        DESTINATION: Can be country, city, town, region
        ATTRACTION: Famous places, monuments, museums, landmarks
        PRODUCT: Tours, activities, experiences with purchase links and media
        
        Use CurrentDateTimeFunction before this function to get current date and time for accurate availability.
        """)
public class ToursFreeTextSearchFunction implements Function<ToursFreeTextSearchFunction.FreeTextSearchRequestWrapper, List<FreeTextSearchResponse>> {

    private final ViatorAuxiliaryServiceAsync viatorAuxiliaryService;
    private final AutoLocationExtractionFunction autoLocationExtractionFunction;

    public ToursFreeTextSearchFunction(ViatorAuxiliaryServiceAsync viatorAuxiliaryService, 
                                     AutoLocationExtractionFunction autoLocationExtractionFunction) {
        this.viatorAuxiliaryService = viatorAuxiliaryService;
        this.autoLocationExtractionFunction = autoLocationExtractionFunction;
    }

    @Override
    @Valid
    public List<FreeTextSearchResponse> apply(FreeTextSearchRequestWrapper requestWrapper) {
        log.info("Performing free text search with term: {}", requestWrapper.getRequest().getSearchTerms());
        try {
            List<FreeTextSearchResponse> response = viatorAuxiliaryService.bulkSearchFreeText(requestWrapper.getAcceptLanguage(), requestWrapper.getRequest());
            log.info("Received free text search response from Viator. Response length: {}", response.toString().length());
            
            // Automatically extract locations from tour results and add to map
            extractLocationsFromTourResults(response);
            
            return response;
        } catch (ViatorAPIException e) {
            log.error("Error performing free text search", e);
            return Collections.singletonList(FreeTextSearchResponse.builder().errorMessage(e.getMessage()).build());
        }
    }
    
    private void extractLocationsFromTourResults(List<FreeTextSearchResponse> responses) {
        try {
            StringBuilder locationMessage = new StringBuilder();
            
            for (FreeTextSearchResponse response : responses) {
                if (response.getProducts() != null && response.getProducts().getResults() != null) {
                    for (FreeTextSearchResponse.ProductSummary product : response.getProducts().getResults()) {
                        if (product.getTitle() != null) {
                            locationMessage.append(product.getTitle()).append(". ");
                        }
                    }
                }
                
                // Extract from attractions as well
                if (response.getAttractions() != null && response.getAttractions().getResults() != null) {
                    for (FreeTextSearchResponse.AttractionResult attraction : response.getAttractions().getResults()) {
                        if (attraction.getName() != null) {
                            locationMessage.append(attraction.getName()).append(". ");
                        }
                        if (attraction.getDestinationName() != null) {
                            locationMessage.append(attraction.getDestinationName()).append(". ");
                        }
                    }
                }
            }
            
            if (locationMessage.length() > 0) {
                log.info("Auto-extracting locations from tour results: {}", locationMessage.toString());
                AutoLocationExtractionFunction.LocationExtractionRequest request = 
                    new AutoLocationExtractionFunction.LocationExtractionRequest(locationMessage.toString(), null);
                String extractionResult = autoLocationExtractionFunction.apply(request);
                log.info("Auto-extraction completed: {}", extractionResult);
            }
            
        } catch (Exception e) {
            log.warn("Failed to auto-extract locations from tour results: {}", e.getMessage());
            // Don't fail the main tour search if location extraction fails
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
