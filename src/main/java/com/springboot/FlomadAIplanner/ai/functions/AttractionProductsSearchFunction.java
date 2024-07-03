package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.viator.AttractionProductsRequest;
import com.springboot.FlomadAIplanner.exceptions.ViatorAPIException;
import com.springboot.FlomadAIplanner.service.viator.ViatorAttractionProductsService;
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
@Description("Retrieves a list of products (tours) related to a specified attraction. " +
        "Use this function to get various tours and activities associated with an attraction ID, " +
        "with optional filtering and sorting. " +
        "This function can be used to find products for cross-selling purposes based on a specific attraction.")
public class AttractionProductsSearchFunction implements AIFunction<AttractionProductsSearchFunction.AttractionProductsSearchRequestWrapper, JsonNode> {

    private final ViatorAttractionProductsService viatorAttractionProductsService;

    @Autowired
    public AttractionProductsSearchFunction(ViatorAttractionProductsService viatorAttractionProductsService) {
        this.viatorAttractionProductsService = viatorAttractionProductsService;
    }

    @Override
    @Valid
    public JsonNode apply(AttractionProductsSearchRequestWrapper requestWrapper) {
        log.info("Performing product search for SEO ID: {}", requestWrapper.getRequest().getSeoId());
        try {
            String response = viatorAttractionProductsService.getAttractionProducts(requestWrapper.getAcceptLanguage(), requestWrapper.getRequest());
            return JsonNodeFactory.instance.textNode(response);
        } catch (ViatorAPIException e) {
            log.error("Error performing product search", e);
            return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttractionProductsSearchRequestWrapper {
        @NotBlank
        @JsonPropertyDescription("Required. Specifies the language into which the natural-language fields in the response will be translated. Example: en-US.")
        private String acceptLanguage;

        @Valid
        @JsonPropertyDescription("The search request containing the SEO ID and optional filters for the attraction products.")
        private AttractionProductsRequest request;
    }
}
