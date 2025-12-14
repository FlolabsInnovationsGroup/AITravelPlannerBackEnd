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
        🎭 CULTURAL EXPERIENCE DISCOVERY ENGINE - Your Gateway to Authentic Travel Adventures

        Discovers and curates authentic tours, activities, and cultural experiences using intelligent natural
        language processing. Transforms travel desires into actionable adventures with local cultural context,
        insider knowledge, and authentic experience recommendations.

        CULTURAL INTELLIGENCE MASTERY:
        🌍 Authentic Experience Curation: Prioritizes local favorites, cultural immersion, authentic traditions
        🎨 Cultural Context Awareness: Understands seasonal festivals, local customs, cultural significance
        🏛️ Historical Intelligence: Connects travelers with UNESCO sites, historical narratives, cultural heritage
        🍷 Culinary Discovery: Authentic food tours, cooking classes, local market experiences, wine traditions
        🎪 Seasonal Experience Optimization: Considers weather, cultural events, seasonal activities, local calendars

        PERFECT FOR SOPHISTICATED CULTURAL DISCOVERY:
        🗨️ Authentic Immersion: "Paris authentic local experiences away from tourist crowds"
        🎭 Cultural Deep Dives: "Traditional Japanese cultural experiences in Kyoto"  
        🍴 Culinary Adventures: "Barcelona authentic tapas tours with local food culture"
        🏛️ Historical Journeys: "Ancient Rome walking tours with archaeological insights"
        🌸 Seasonal Specialties: "Japan cherry blossom festivals and hanami experiences"
        🎨 Art & Heritage: "Florence Renaissance art tours with local art historians"
        
        MULTI-DESTINATION INTELLIGENCE:
        ⚡ Strategic Efficiency: Process ALL destinations in ONE intelligent call for optimal results
        🗺️ Cultural Route Planning: "Mediterranean cultural tour: Greece, Italy, Spain traditions"
        🌍 Regional Expertise: "Southeast Asia adventure: Thailand, Vietnam, Cambodia experiences"
        🏔️ Thematic Journeys: "Alpine culture tour: Switzerland, Austria, Bavaria traditions"
        
        ADVANCED SEARCH CAPABILITIES:
        - Cultural Event Integration: Links activities to festivals, celebrations, seasonal traditions
        - Local Insider Access: Discovers hidden gems, local guide recommendations, authentic venues
        - Authentic Experience Filtering: Prioritizes cultural immersion over tourist attractions
        - Budget & Quality Intelligence: Balances authentic experiences with traveler preferences
        - Accessibility Awareness: Considers mobility needs, age groups, cultural sensitivities
        
        CRITICAL SEARCH METHODOLOGY:
        ✅ OPTIMAL: ["Paris authentic local culinary experiences"] - Combined location + cultural context
        ❌ INEFFICIENT: ["culinary tours", "Paris"] - Separate terms lose cultural intelligence
        
        SEAMLESS MAP INTEGRATION INTELLIGENCE:
        🔄 Automatic Location Discovery: Extracts specific venues from tour descriptions  
        📍 Cultural Site Mapping: Maps historical sites, cultural venues, authentic local spots
        🗺️ Experience Visualization: Shows tour routes, cultural districts, authentic neighborhoods
        ⚡ Real-time Sync: Instant map updates with cultural context and timing recommendations
        
        CULTURAL WORKFLOW EXCELLENCE:
        1. 🎯 Intelligent search with cultural context and seasonal awareness
        2. 🌟 Authentic experience curation prioritizing local cultural immersion
        3. 📍 Automatic venue extraction and map visualization with cultural insights
        4. 🎭 Cultural preparation tips, local etiquette, and insider knowledge delivery
        
        TRAVEL TIMING INTELLIGENCE: Use with CurrentDateTimeFunction for seasonal availability, 
        cultural calendar awareness, and optimal booking timing for authentic experiences.
        
        Returns: Curated authentic experiences with cultural context, insider tips, and local cultural intelligence
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
                    new AutoLocationExtractionFunction.LocationExtractionRequest();
                request.setMessage(locationMessage.toString());
                request.setUserQuery(null);
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
