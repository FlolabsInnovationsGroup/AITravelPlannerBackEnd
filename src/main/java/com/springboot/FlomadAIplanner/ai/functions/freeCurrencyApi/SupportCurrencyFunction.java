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

import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
@Description("""
        Retrieves comprehensive list of supported currencies available for exchange rate conversion. 
        Essential for showing travelers what currencies are available for their budget planning.
        
        CORE PURPOSE: Currency availability reference for travel planning
        
        KEY FEATURES:
        - Complete list of supported currencies
        - Currency codes and names
        - Exchange rate availability confirmation
        - Travel destination currency support
        
        ESSENTIAL TRIGGER: Call when travelers ask about currency availability or supported currencies
        
        PERFECT FOR:
        - "What currencies can I convert?"
        - "Is Thai Baht supported?"  
        - "Show me all available currencies"
        - Pre-conversion currency validation
        
        WORKFLOW INTEGRATION: Use before LatestCurrencyFunction or HistoryCurrencyFunction to validate currency codes
        
        Examples: Currency availability inquiries, supported currency lists, travel destination currency validation
        """)
public class SupportCurrencyFunction implements Function<SupportCurrencyFunction.SupportCurrencyRequest, JsonNode> {

    private final FreeCurrencyService freeCurrencyService;

    public SupportCurrencyFunction(FreeCurrencyService freeCurrencyService) {
        this.freeCurrencyService = freeCurrencyService;
    }

    @Override
    public JsonNode apply(SupportCurrencyRequest request) {
        log.info("Performing Get Supported Currency Request");
        try {
            List<String> currenciesList = request.getCurrencies() != null ? request.getCurrencies() : List.of();
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
        @JsonPropertyDescription("A list of currency codes to retrieve support information for")
        private List<String> currencies;  // A comma-separated string of currency codes
    }
}
