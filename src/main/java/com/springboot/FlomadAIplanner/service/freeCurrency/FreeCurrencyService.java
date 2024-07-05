package com.springboot.FlomadAIplanner.service.freeCurrency;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.client.json.Json;
import com.springboot.FlomadAIplanner.exceptions.FreeCurrencyException;
import com.springboot.FlomadAIplanner.exceptions.ViatorAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;

import java.util.Date;

@Slf4j
@Service
public class FreeCurrencyService {

    private final FreeCurrencyClient freeCurrencyClient;

    public FreeCurrencyService(FreeCurrencyClient freeCurrencyClient) {
        this.freeCurrencyClient = freeCurrencyClient;
    }

    public JsonNode getSupportedCurrencies(String currencies) throws FreeCurrencyException {
        try {
            log.info("Search for Supported Currencies");
            JsonNode response;
            if (currencies != null && !currencies.equals("")) {
                response = freeCurrencyClient.getSupportedCurrencies(currencies);
            } else {
                response = freeCurrencyClient.getSupportedCurrencies();
            }
            log.info("Received Response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error performing free text search", e);
            throw new FreeCurrencyException("Error performing search Supported Currencies", e);
        }
    }

    public JsonNode getStatus() throws FreeCurrencyException {
        try {
            log.info("Getting Free Currencies Status");
            JsonNode response = freeCurrencyClient.getStatus();
            log.info("Received Response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error performing Getting Free Currencies Status", e);
            throw new FreeCurrencyException("Error performing Getting Free Currencies Status", e);
        }
    }

    public JsonNode getLatestCurrency(String baseCurrency, String currencies) throws FreeCurrencyException {
        try {
            log.info("Search for Latest Currencies:{} with Base Currency:{}", currencies, baseCurrency);
            JsonNode response;
            if (currencies != null && !currencies.equals("") && baseCurrency != null) {
                response = freeCurrencyClient.getLatestCurrency(baseCurrency, currencies);
            } else if (baseCurrency != null) {
                response = freeCurrencyClient.getLatestCurrency(baseCurrency);
            } else if (currencies != null && !currencies.equals("")) {
                response = freeCurrencyClient.getUSDLatestCurrency(currencies);
            } else {
                response = freeCurrencyClient.getUSDLatestCurrency();
            }
            log.info("Received Response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error performing Searching Latest Currencies", e);
            throw new FreeCurrencyException("Error performing search Latest Currencies", e);
        }
    }

    public JsonNode getHistoricalCurrency(String date, String baseCurrency, String currencies) throws FreeCurrencyException {
        try {
            log.info("Search for Historical Currencies:{} with Base Currency:{}", currencies, baseCurrency);
            JsonNode response;
            if (currencies != null && !currencies.equals("") && baseCurrency != null) {
                response = freeCurrencyClient.getHistoricalCurrency(date, baseCurrency, currencies);
            } else if (baseCurrency != null) {
                response = freeCurrencyClient.getHistoricalCurrency(date, baseCurrency);
            } else if (currencies != null && !currencies.equals("")) {
                response = freeCurrencyClient.getUSDHistoricalCurrency(date, currencies);
            } else {
                response = freeCurrencyClient.getUSDHistoricalCurrency(date);
            }
            log.info("Received Response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error performing Searching Historical Currencies", e);
            throw new FreeCurrencyException("Error performing search Historical Currencies", e);
        }
    }

//    public static void main(String[] args) {
//        String[] temp = {};
//        System.out.println(temp.length);
//    }
}
