package com.springboot.FlomadAIplanner.service.freeCurrency;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.springboot.FlomadAIplanner.exceptions.api.FreeCurrencyAPIException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public JsonNode getSupportedCurrencies(List<String> currencies) throws FreeCurrencyAPIException {
        try {
            log.info("Search for Supported Currencies");
            JsonNode response;
            String currenciesStr = (currencies != null && !currencies.isEmpty())
                    ? StringUtils.join(currencies, ",")
                    : null;

            if (currenciesStr != null) {
                response = freeCurrencyClient.getSupportedCurrencies(currenciesStr);
            } else {
                response = freeCurrencyClient.getSupportedCurrencies();
            }
            log.info("Received Response: {}", response);
            return response;
        } catch (FeignException e) {
            if (e.status() == 422 && e.getMessage().contains("The selected currencies is invalid.")) {
                ObjectNode errorNode = JsonNodeFactory.instance.objectNode();
                ObjectNode errorsNode = errorNode.putObject("errors");
                errorsNode.put("message", "Validation error")
                        .put("currencies", "The selected currencies is invalid.")
                        .put("code", StringUtils.join(currencies, ","));
                log.info("Invalid Currency: {}", errorNode);
                return errorNode;
            } else {
                log.error("Error performing free text search", e);
                throw new FreeCurrencyAPIException("Error performing search Supported Currencies", e);
            }
        } catch (Exception e) {
            log.error("Error performing free text search", e);
            throw new FreeCurrencyAPIException("Error performing search Supported Currencies", e);
        }
    }

    /**
     * Get status of usage of the FreeCurrency API, there is a limit of 5000 requests/months.
     *
     * @return status of usage in JsonNode Type
     */
    public JsonNode getStatus() throws FreeCurrencyAPIException {
        try {
            log.info("Getting Free Currencies Status");
            JsonNode response = freeCurrencyClient.getStatus();
            log.info("Received Response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error performing Getting Free Currencies Status", e);
            throw new FreeCurrencyAPIException("Error performing Getting Free Currencies Status", e);
        }
    }

    /**
     * Get latest currencies exchange rate against base currency.
     *
     * @param baseCurrency default USD
     * @param currencies   a list of currency code separated by ",", default select all currencies.
     * @return JsonNode type result. Do not contain baseCurrency if it is not in currencies.
     */
    public JsonNode getLatestCurrency(String baseCurrency, List<String> currencies) throws FreeCurrencyAPIException {
        try {
            log.info("Search for Latest Currencies:{} with Base Currency:{}", currencies, baseCurrency);
            JsonNode response;
            String currenciesStr = (currencies != null && !currencies.isEmpty())
                    ? StringUtils.join(currencies, ",")
                    : null;

            if (currenciesStr != null && baseCurrency != null) {
                response = freeCurrencyClient.getLatestCurrency(baseCurrency, currenciesStr);
            } else if (baseCurrency != null) {
                response = freeCurrencyClient.getLatestCurrency(baseCurrency);
            } else if (currenciesStr != null) {
                response = freeCurrencyClient.getUSDLatestCurrency(currenciesStr);
            } else {
                response = freeCurrencyClient.getUSDLatestCurrency();
            }
            log.info("Received Response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error performing Searching Latest Currencies", e);
            throw new FreeCurrencyAPIException("Error performing search Latest Currencies", e);
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
    public JsonNode getHistoricalCurrency(String date, String baseCurrency, List<String> currencies) throws FreeCurrencyAPIException {
        try {
            log.info("Search for Historical Currencies:{} with Base Currency:{}", currencies, baseCurrency);
            JsonNode response;
            String currenciesString = StringUtils.join(currencies, ",");
            if (currencies != null && !currencies.isEmpty() && baseCurrency != null) {
                response = freeCurrencyClient.getHistoricalCurrency(date, baseCurrency, currenciesString);
            } else if (baseCurrency != null) {
                response = freeCurrencyClient.getHistoricalCurrency(date, baseCurrency);
            } else if (currencies != null && !currencies.isEmpty()) {
                response = freeCurrencyClient.getUSDHistoricalCurrency(date, currenciesString);
            } else {
                response = freeCurrencyClient.getUSDHistoricalCurrency(date);
            }
            log.info("Received Response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error performing Searching Historical Currencies", e);
            throw new FreeCurrencyAPIException("Error performing search Historical Currencies", e);
        }
    }
}
