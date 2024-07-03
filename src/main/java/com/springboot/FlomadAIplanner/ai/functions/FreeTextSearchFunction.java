package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.viator.FreeTextSearchRequest;
import com.springboot.FlomadAIplanner.exceptions.ViatorAPIException;
import com.springboot.FlomadAIplanner.service.viator.ViatorAuxiliaryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Description("Performs a free text search for Viator products, attractions, and destinations. " +
        "Use this function to search for various activities, tours, and experiences available." +
        "Mostly could be called to find out general information about a destination or a specific tour (payed) activities." +
        "Or to find out ids of the activities, destinations or products that could be used in other functions. " +
        "I think this function should be used mostly for required destination search")
public class FreeTextSearchFunction implements AIFunction<FreeTextSearchFunction.FreeTextSearchRequestWrapper, JsonNode> {

    private final ViatorAuxiliaryService viatorAuxiliaryService;

    @Autowired
    public FreeTextSearchFunction(ViatorAuxiliaryService viatorAuxiliaryService) {
        this.viatorAuxiliaryService = viatorAuxiliaryService;
    }

    @Override
    @Valid
    public JsonNode apply(FreeTextSearchRequestWrapper requestWrapper) {
        log.info("Performing free text search with term: {}", requestWrapper.getRequest().getSearchTerm());
        try {
            return viatorAuxiliaryService.searchFreeText(requestWrapper.getAcceptLanguage(), requestWrapper.getRequest());
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
        @JsonPropertyDescription("Required. Specifies the language into which the natural-language fields in the response will be translated if needed. Example: en-US. By default use en-US")
        private String acceptLanguage;

        @Valid
        @JsonPropertyDescription("The free text search request containing the search term and filters for the search.")
        private FreeTextSearchRequest request;
    }

}
