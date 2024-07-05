package com.springboot.FlomadAIplanner.controller.freeCurrency;


import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.exceptions.AmadeusAPIException;
import com.springboot.FlomadAIplanner.exceptions.FreeCurrencyException;
import com.springboot.FlomadAIplanner.service.freeCurrency.FreeCurrencyService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api/freeCurrency")
public class FreeCurrencyController {

    private final FreeCurrencyService freeCurrencyService;

    @Autowired
    public FreeCurrencyController(FreeCurrencyService freeCurrencyService) {
        this.freeCurrencyService = freeCurrencyService;
    }

    @GetMapping("/currencies")
    public ResponseEntity<JsonNode> getSupportCurrencies(@RequestParam @Nullable String currencies) throws FreeCurrencyException {
        log.info("Received currencies: {}", (Object) currencies);

        JsonNode response = freeCurrencyService.getSupportedCurrencies(currencies);

        log.info("Received response from FreeCurrency API");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<JsonNode> getStatus() throws FreeCurrencyException {
        log.info("Performing Get FreeCurrency API Status");

        JsonNode response = freeCurrencyService.getStatus();

        log.info("Received response from FreeCurrency API");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest")
    public ResponseEntity<JsonNode> getLatestCurrency(@RequestParam @Nullable String base,
                                                     @RequestParam @Nullable String currencies) throws FreeCurrencyException {
        log.info("Received currencies: {}, base currencies: {}", currencies, base);

        JsonNode response = freeCurrencyService.getLatestCurrency(base, currencies);

        log.info("Received response from FreeCurrency API");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<JsonNode> getHistoricalCurrency(@RequestParam @NotNull Date date,
                                                          @RequestParam @Nullable String base,
                                                          @RequestParam @Nullable String currencies) throws FreeCurrencyException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(date);
        log.info("Received date: {} currencies: {}, base currencies: {}", formattedDate, currencies, base);

        JsonNode response = freeCurrencyService.getHistoricalCurrency(formattedDate, base, currencies);

        log.info("Received response from FreeCurrency API");
        return ResponseEntity.ok(response);
    }
}
