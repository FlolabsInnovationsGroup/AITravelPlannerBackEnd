package com.springboot.FlomadAIplanner.ai.functions.freeCurrencyApi;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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
import java.util.function.Function;

@Slf4j
@Component
@Description("""
        Retrieves historical exchange rates for specific dates, enabling travelers to analyze currency trends 
        and make informed decisions about timing their currency exchanges.
        
        CORE PURPOSE: Historical currency rate analysis for strategic travel planning
        
        KEY FEATURES:
        - Historical exchange rates for specific dates
        - Currency trend analysis capability
        - Base currency to multiple targets on past dates
        - Travel timing optimization support
        
        AUTOMATIC OPTIMIZATION: If base currency not in currencies list, it will be automatically added
        
        PERFECT FOR:
        - "What was the USD to EUR rate last month?"
        - "Show me exchange rates for my travel dates" 
        - Currency trend analysis for travel timing
        - Historical budget planning and comparisons
        
        ERROR HANDLING: If function returns error node, do NOT retry with same input parameters
        
        USAGE PATTERN: Use when travelers ask about past exchange rates, currency trends, or specific date conversions
        
        Examples: Historical rate inquiries, travel timing decisions, currency trend analysis for past periods
        """)
public class HistoryCurrencyFunction implements Function<HistoryCurrencyFunction.HistoryCurrencyRequest, JsonNode> {

    private final FreeCurrencyService freeCurrencyService;

    @Autowired
    public HistoryCurrencyFunction(FreeCurrencyService freeCurrencyService) {
        this.freeCurrencyService = freeCurrencyService;
    }

    @Override
    public JsonNode apply(HistoryCurrencyFunction.HistoryCurrencyRequest request) {
        log.info("Performing Get History Currency Request");
        try {
            List<String> currenciesList = Arrays.asList(request.getCurrencies().split(","));
            JsonNode mes = freeCurrencyService.getSupportedCurrencies(currenciesList);
            if (mes.has("errors")) {
                return mes;
            }
            return freeCurrencyService.getHistoricalCurrency(request.getDate(), request.getBaseCurrency(), currenciesList);
        } catch (Exception e) {
            log.error("Error Getting History Currency Request", e);
            return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryCurrencyRequest {

        @JsonPropertyDescription("Required. Provides the date to retrieve historical rates from. " +
                "The format must be yyyy-mm-dd, for example: 2021-12-31.")
        private String date;

        @JsonPropertyDescription("Optional. By default, use USD as base currency. " +
                "Provides a base currency with which to search for exchange rates.")
        private String baseCurrency;
        @JsonPropertyDescription("Optional. By default, returns all supported currencies. " +
                "Provide a String type list of currencies separated by ',' to check for support in currency exchange rate conversion.")
        private String currencies;
    }
}
