package com.springboot.FlomadAIplanner.ai.functions.freeCurrencyApi;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.ai.functions.AIFunction;
import com.springboot.FlomadAIplanner.service.freeCurrency.FreeCurrencyService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Description("Retrieves supported currencies available for exchange rate conversion. " +
        "Use this function to retrieve all supported currencies available for exchange rate conversion. " +
        "It is essential to call this function before using latestCurrencyFunction and historyCurrencyFunction to " +
        "ensure that the desired currencies are supported. " +
        "If any of the currencies are not supported, do not proceed with latestCurrencyFunction and historyCurrencyFunction.")
public class SupportCurrencyFunction implements AIFunction<SupportCurrencyFunction.SupportCurrencyRequest, JsonNode> {

    private final FreeCurrencyService freeCurrencyService;

    @Autowired
    public SupportCurrencyFunction(FreeCurrencyService freeCurrencyService) {
        this.freeCurrencyService = freeCurrencyService;
    }

    @Override
    public JsonNode apply(SupportCurrencyRequest request) {
        log.info("Performing Get Supported Currency Request");
        try {
            return freeCurrencyService.getSupportedCurrencies(request.currencies);
        } catch (Exception e) {
            log.error("Error Getting Supported Currency Request", e);
            return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupportCurrencyRequest {
        @JsonPropertyDescription("Optional. By default, returns all supported currencies. " +
                "Provide a String type list of currencies separated by ',' to check for support in currency exchange rate conversion.")
        private String currencies;
    }
}
