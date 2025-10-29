package com.springboot.FlomadAIplanner.ai.functions.freeCurrencyApi;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.service.freeCurrency.FreeCurrencyService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
@Description("""
        Retrieves current exchange rates for currency conversion calculations. Provides real-time conversion
        rates between base currency and multiple target currencies for travel budgeting.
        CORE PURPOSE: Real-time currency exchange rate conversion for travel planning
        
        KEY FEATURES:
        - Latest market exchange rates
        - Base currency to multiple target currencies
        - Travel budgeting support
        - Multi-currency comparison
        
        AUTOMATIC OPTIMIZATION: If base currency not in currencies list, it will be automatically added
        
        PERFECT FOR:
        - "Convert 500 USD to EUR, GBP, JPY"  
        - "What's the current exchange rate for USD to EUR?"
        - Travel budget planning across destinations
        - Multi-currency price comparisons
        
        ERROR HANDLING: If function returns error node, do NOT retry with same input parameters
        
        USAGE PATTERN: Use whenever travelers ask about currency exchange, conversion rates, or budget planning
        
        Examples: Currency exchange inquiries, travel budget calculations, price conversions for international trips
        """)
public class LatestCurrencyFunction implements Function<LatestCurrencyFunction.LatestCurrencyRequest, JsonNode> {

    private final FreeCurrencyService freeCurrencyService;

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