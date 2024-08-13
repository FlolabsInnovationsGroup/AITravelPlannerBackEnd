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
import java.util.List;

@Slf4j
@Component
@Description("Retrieves supported currencies available for exchange rate conversion. " +
        "Use this function to retrieve selected supported currencies available for exchange rate conversion. " +
        "It is essential to call this function when people ask what currencies are available for exchange. ")
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
                    List<String> currenciesList = request.currencies != null ? List.of(request.currencies.split(",")) : List.of();
                    return freeCurrencyService.getSupportedCurrencies(currenciesList);
                } catch (Exception e) {
                    log.error("Error Getting Supported Currency Request", e);
                    return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
                }
            }
        
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class SupportCurrencyRequest {
                private String currencies;  // A comma-separated string of currency codes
            }
        }
