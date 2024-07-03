package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.viator.AttractionsRequest;
import com.springboot.FlomadAIplanner.exceptions.ViatorAPIException;
import com.springboot.FlomadAIplanner.service.viator.ViatorAttractionsService;
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
@Description("Retrieves a list of attractions for a given destination. " +
        "Use this function to get various attractions for a specified destination ID, " +
        "with optional filtering and sorting. destination ID could be found from the FreeTextSearchFunction response only." +
        "So, this function must be called after receiving a result from FreeTextSearchFunction to get a more " +
        "detailed list of attractions for a specific destination if needed.")
public class AttractionsSearchFunction implements AIFunction<AttractionsSearchFunction.AttractionsSearchRequestWrapper, JsonNode> {
    private final ViatorAttractionsService viatorAttractionsService;

    @Autowired
    public AttractionsSearchFunction(ViatorAttractionsService viatorAttractionsService) {
        this.viatorAttractionsService = viatorAttractionsService;
    }

    @Override
    @Valid
    public JsonNode apply(AttractionsSearchRequestWrapper requestWrapper) {
        log.info("Getting attractions for destination: {}", requestWrapper.getRequest().getDestId());
        try {
            String response = viatorAttractionsService.getAttractions(requestWrapper.getAcceptLanguage(), requestWrapper.getRequest());
            return JsonNodeFactory.instance.textNode(response);
        } catch (ViatorAPIException e) {
            log.error("Error getting attractions", e);
            return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttractionsSearchRequestWrapper {
        @NotBlank
        @JsonPropertyDescription("Required. Specifies the language into which the natural-language fields in the response will be translated. Example: en-US.")
        private String acceptLanguage;

        @Valid
        @JsonPropertyDescription("The attractions search request containing the destination ID and optional filters.")
        private AttractionsRequest request;
    }
}
