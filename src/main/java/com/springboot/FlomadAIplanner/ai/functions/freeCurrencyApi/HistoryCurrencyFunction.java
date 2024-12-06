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
@Description("Retrieves the historical currencies exchange rate conversion for base currency against currencies list" +
        "Use this function to retrieve the historical currencies exchange rate conversion on a certain date. " +
        "Use this function whenever customer asks about currency exchange on a specific date. " +
        "If the base currency is not provided in currencies list, you should add base currency into currencies list." +
        "If this function returns errors node, Do not proceed this function with the same input again.")
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
