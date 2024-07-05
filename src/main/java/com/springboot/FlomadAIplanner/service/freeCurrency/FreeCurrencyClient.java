package com.springboot.FlomadAIplanner.service.freeCurrency;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.service.freeCurrency.config.FreeCurrencyConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "freeCurrencyClient", url = "${freeCurrency.api.baseurl}", configuration = FreeCurrencyConfig.class)
public interface FreeCurrencyClient {

    @GetMapping(value = "/currencies", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getSupportedCurrencies();

    @GetMapping(value = "/currencies", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getSupportedCurrencies(@RequestParam("currencies") String currencies);

    @GetMapping(value = "/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getStatus();

    @GetMapping(value = "/latest", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getLatestCurrency(@RequestParam("base_currency") String baseCurrency,
                               @RequestParam("currencies") String currencies);

    @GetMapping(value = "/latest", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getLatestCurrency(@RequestParam("base_currency") String baseCurrency);

    @GetMapping(value = "/latest", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getUSDLatestCurrency(@RequestParam("currencies") String currencies);

    @GetMapping(value = "/latest", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getUSDLatestCurrency();

    @GetMapping(value = "/historical", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getHistoricalCurrency(@RequestParam("date") String date,
                                   @RequestParam("base_currency") String baseCurrency,
                                   @RequestParam("currencies") String currencies);

    @GetMapping(value = "/historical", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getHistoricalCurrency(@RequestParam("date") String date,
                                   @RequestParam("base_currency") String baseCurrency);

    @GetMapping(value = "/historical", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getUSDHistoricalCurrency(@RequestParam("date") String date);

    @GetMapping(value = "/historical", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getUSDHistoricalCurrency(@RequestParam("date") String date,
                                      @RequestParam("currencies") String currencies);
}
