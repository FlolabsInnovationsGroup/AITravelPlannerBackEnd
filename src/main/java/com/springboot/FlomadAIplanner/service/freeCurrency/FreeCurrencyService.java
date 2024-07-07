package com.springboot.FlomadAIplanner.service.freeCurrency;


import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.exceptions.FreeCurrencyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FreeCurrencyService {

    private final FreeCurrencyClient freeCurrencyClient;

    public FreeCurrencyService(FreeCurrencyClient freeCurrencyClient) {
        this.freeCurrencyClient = freeCurrencyClient;
    }

    /**
     * Get supported currencies from FreeCurrencyClient. If currencies is null, return all supported currencies.
     * Else, return the supported currencies in the parameter.
     *
     * @param currencies a list of currency code separated by ",", default select all currencies.
     * @return JsonNode type result.
     */
    public JsonNode getSupportedCurrencies(String currencies) throws FreeCurrencyException {
        try {
            log.info("Search for Supported Currencies");
            JsonNode response;
            if (currencies != null && !currencies.isEmpty()) {
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

    /**
     * Get status of usage of the FreeCurrency API, there is a limit of 5000 requests/months.
     *
     * @return status of usage in JsonNode Type
     */
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

    /**
     * Get latest currencies exchange rate against base currency.
     *
     * @param baseCurrency default USD
     * @param currencies   a list of currency code separated by ",", default select all currencies.
     * @return JsonNode type result. Do not contain baseCurrency if it is not in currencies.
     */
    public JsonNode getLatestCurrency(String baseCurrency, String currencies) throws FreeCurrencyException {
        try {
            log.info("Search for Latest Currencies:{} with Base Currency:{}", currencies, baseCurrency);
            JsonNode response;
            if (currencies != null && !currencies.isEmpty() && baseCurrency != null) {
                response = freeCurrencyClient.getLatestCurrency(baseCurrency, currencies);
            } else if (baseCurrency != null) {
                response = freeCurrencyClient.getLatestCurrency(baseCurrency);
            } else if (currencies != null && !currencies.isEmpty()) {
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

    /**
     * Get historical currencies exchange rate against base currency on a certain date.
     *
     * @param date         Required. must in format yyyy-mm-dd, for example: 2021-12-31
     * @param baseCurrency default USD
     * @param currencies   a list of currency code separated by ",", default select all currencies.
     * @return JsonNode type result. Do not contain baseCurrency if it is not in currencies.
     */
    public JsonNode getHistoricalCurrency(String date, String baseCurrency, String currencies) throws FreeCurrencyException {
        try {
            log.info("Search for Historical Currencies:{} with Base Currency:{}", currencies, baseCurrency);
            JsonNode response;
            if (currencies != null && !currencies.isEmpty() && baseCurrency != null) {
                response = freeCurrencyClient.getHistoricalCurrency(date, baseCurrency, currencies);
            } else if (baseCurrency != null) {
                response = freeCurrencyClient.getHistoricalCurrency(date, baseCurrency);
            } else if (currencies != null && !currencies.isEmpty()) {
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

}
