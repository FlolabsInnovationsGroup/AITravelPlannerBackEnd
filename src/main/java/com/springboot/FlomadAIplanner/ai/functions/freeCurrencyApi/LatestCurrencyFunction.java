package com.springboot.FlomadAIplanner.ai.functions.freeCurrencyApi;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.ai.functions.AIFunction;
import com.springboot.FlomadAIplanner.service.freeCurrency.FreeCurrencyService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Description("Retrieves the latest currencies exchange rate conversion for base currency against currencies list" +
        "Use this function to retrieve the latest currencies exchange rate conversion. " +
        "Use this function whenever customer asks about currency exchange. " +
        "If the base currency is not provided in currencies list, you should add base currency into currencies list." +
        "If this function returns errors node, Do not proceed this function with the same input again.")
public class LatestCurrencyFunction implements AIFunction<LatestCurrencyFunction.LatestCurrencyRequest, JsonNode> {

    private final FreeCurrencyService freeCurrencyService;

    @Autowired
    public LatestCurrencyFunction(FreeCurrencyService freeCurrencyService) {
        this.freeCurrencyService = freeCurrencyService;
    }

    @Override
    public JsonNode apply(LatestCurrencyFunction.LatestCurrencyRequest request) {
        log.info("Performing Get History Currency Request");
        try {
            List<String> currenciesList = Arrays.asList(request.getCurrencies().split(","));
            JsonNode mes = freeCurrencyService.getSupportedCurrencies(currenciesList);
            if (mes.has("errors")) {
                return mes;
            }
            return freeCurrencyService.getLatestCurrency(request.baseCurrency, currenciesList);
        } catch (Exception e) {
            log.error("Error Getting Latest Currency Request", e);
            return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LatestCurrencyRequest {

        @JsonPropertyDescription("Optional. By default, use USD as base currency. " +
                "Provides a base currency with which to search for exchange rates.")
        private String baseCurrency;
        @JsonPropertyDescription("Optional. By default, returns all supported currencies. " +
                "Provide a List type of currencies to check for support in currency exchange rate conversion.")
        private String currencies;
    }
}