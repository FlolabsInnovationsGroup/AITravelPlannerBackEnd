package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.viator.BulkFreeTextSearchRequest;
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

@Slf4j
@Component
@Description("""
        Use this function to perform a free text search for TOURS to the required destinations (countries, cities, famous places, etc.).
        Searching for Viator PRODUCTS, ATTRACTIONS, and DESTINATIONS.
        Use this function every time if you need to search for various activities, tours, and experiences available for each different place required, in case needed to do so.
        Recommended to use for search for tours in all required places in one call. SearchTerms - [1,∞)
        Mostly could be called to find out general information about a tours (payed activities)\s
        or famous places available in the desired destination.
        DESTINATION could be a country, city, town, etc.
        ATTRACTION could be a famous place, monument, museum, etc.
        PRODUCT could be a tour, activity, experience, etc.
        'productUrl' - is actually the link to the tour purchase and description, media.
        P.S. Use CurrentDateTimeFunction before this function to get the current date and time in the real world.""")
public class ToursFreeTextSearchFunction implements AIFunction<ToursFreeTextSearchFunction.FreeTextSearchRequestWrapper, JsonNode> {

    private final ViatorAuxiliaryServiceAsync viatorAuxiliaryService;

    @Autowired
    public ToursFreeTextSearchFunction(ViatorAuxiliaryServiceAsync viatorAuxiliaryService) {
        this.viatorAuxiliaryService = viatorAuxiliaryService;
    }

    @Override
    @Valid
    public JsonNode apply(FreeTextSearchRequestWrapper requestWrapper) {
        log.info("Performing free text search with term: {}", requestWrapper.getRequest().getSearchTerms());
        try {
            return viatorAuxiliaryService.bulkSearchFreeText(requestWrapper.getAcceptLanguage(), requestWrapper.getRequest());
        } catch (ViatorAPIException e) {
            log.error("Error performing free text search", e);
            return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FreeTextSearchRequestWrapper {

        @NotBlank
        @JsonPropertyDescription("REQUIRED. Specifies the language into which the natural-language fields in the response will be translated if needed. Example: en-US. By default use en-US")
        private String acceptLanguage;

        @Valid
        @NotNull
        @JsonPropertyDescription("REQUIRED. The free text search request containing the search term and filters for the search.")
        private BulkFreeTextSearchRequest request;
    }

}
