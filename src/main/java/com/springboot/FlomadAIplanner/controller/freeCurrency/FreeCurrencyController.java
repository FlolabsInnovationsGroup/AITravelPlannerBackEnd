package com.springboot.FlomadAIplanner.controller.freeCurrency;


import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.exceptions.api.FreeCurrencyAPIException;
import com.springboot.FlomadAIplanner.service.freeCurrency.FreeCurrencyService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;

@Slf4j
@RestController
@RequestMapping("/api/freeCurrency")
public class FreeCurrencyController {

    private final FreeCurrencyService freeCurrencyService;

    @Autowired
    public FreeCurrencyController(FreeCurrencyService freeCurrencyService) {
        this.freeCurrencyService = freeCurrencyService;
    }

    /**
     * sample request parameters: /currencies?currencies=USD,CAD,CNY,EUR
     *
     * @param currencies currencies list of currency code separated by ","
     * @return Supported currencies
     */
    @GetMapping("/currencies")
    public ResponseEntity<JsonNode> getSupportCurrencies(@RequestParam @Nullable String currencies) throws FreeCurrencyAPIException {
        log.info("Received currencies: {}", currencies);

        JsonNode response = freeCurrencyService.getSupportedCurrencies(currencies);

        log.info("Received response from FreeCurrency API");
        return ResponseEntity.ok(response);
    }

    /**
     * sample request parameters: /status
     *
     * @return usage of API and remaining attempts
     */
    @GetMapping("/status")
    public ResponseEntity<JsonNode> getStatus() throws FreeCurrencyAPIException {
        log.info("Performing Get FreeCurrency API Status");

        JsonNode response = freeCurrencyService.getStatus();

        log.info("Received response from FreeCurrency API");
        return ResponseEntity.ok(response);
    }

    /**
     * sample request parameters: /latest?base=EUR&currencies=USD,CAD,CNY,EUR.
     *
     * @param base       base currency, default USD
     * @param currencies currencies list of currency code separated by ",", default all currencies
     * @return Latest Currencies Exchange rate
     */
    @GetMapping("/latest")
    public ResponseEntity<JsonNode> getLatestCurrency(@RequestParam @Nullable String base,
                                                      @RequestParam @Nullable String currencies) throws FreeCurrencyAPIException {
        log.info("Received currencies: {}, base currencies: {}", currencies, base);

        JsonNode response = freeCurrencyService.getLatestCurrency(base, currencies);

        log.info("Received response from FreeCurrency API");
        return ResponseEntity.ok(response);
    }

    /**
     * sample request parameters: /history?base=EUR&currencies=USD,CAD,CNY,EUR&date=2022-12-31.
     *
     * @param date       must be in format: 2021-12-31
     * @param base       base currency, default USD
     * @param currencies currencies list of currency code separated by ",", default all currencies
     * @return Json result
     */
    @GetMapping("/history")
    public ResponseEntity<JsonNode> getHistoricalCurrency(@RequestParam @NotNull String date,
                                                          @RequestParam @Nullable String base,
                                                          @RequestParam @Nullable String currencies) throws FreeCurrencyAPIException {
        log.info("Received date: {} currencies: {}, base currencies: {}", date, currencies, base);

        JsonNode response = freeCurrencyService.getHistoricalCurrency(date, base, currencies);

        log.info("Received response from FreeCurrency API");
        return ResponseEntity.ok(response);
    }
}
