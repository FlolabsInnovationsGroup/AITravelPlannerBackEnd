package com.springboot.FlomadAIplanner.service.freeCurrency;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.service.freeCurrency.config.FreeCurrencyConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "freeCurrencyClient", url = "${freeCurrency.api.baseurl}", configuration = FreeCurrencyConfig.class)
public interface FreeCurrencyClient {

    /**
     * Implementation of the GET method to get supported currencies
     * without parameters.
     *
     * @description <a href="https://freecurrencyapi.com/docs/currencies"/>
     */
    @GetMapping(value = "/currencies", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getSupportedCurrencies();

    /**
     * Implementation of the GET method to get supported currencies
     * with:
     *      parameter currencies: a list of currency code separated by ",".
     *
     * @description <a href="https://freecurrencyapi.com/docs/currencies"/>
     */
    @GetMapping(value = "/currencies", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getSupportedCurrencies(@RequestParam("currencies") String currencies);

    /**
     * Implementation of the GET method to get API usage Status
     *
     * @description <a href="https://freecurrencyapi.com/docs/status"/>
     */
    @GetMapping(value = "/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getStatus();

    /**
     * Implementation of the GET method to get the latest currencies exchange rate
     * with:
     *      parameter base_currency: a currency with which to search for exchange rates.
     *      parameter currencies: a list of currency code separated by ",".
     *
     * @description <a href="https://freecurrencyapi.com/docs/latest"/>
     */
    @GetMapping(value = "/latest", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getLatestCurrency(@RequestParam("base_currency") String baseCurrency,
                               @RequestParam("currencies") String currencies);

    /**
     * Implementation of the GET method to get the latest currencies exchange rate
     * with:
     *      parameter base_currency: a currency with which to search for exchange rates.
     *
     * @description <a href="https://freecurrencyapi.com/docs/latest"/>
     */
    @GetMapping(value = "/latest", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getLatestCurrency(@RequestParam("base_currency") String baseCurrency);

    /**
     * Implementation of the GET method to get the latest currencies exchange rate
     * with:
     *      parameter currencies: a list of currency code separated by ",".
     *
     * @description <a href="https://freecurrencyapi.com/docs/latest"/>
     */
    @GetMapping(value = "/latest", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getUSDLatestCurrency(@RequestParam("currencies") String currencies);

    /**
     * Implementation of the GET method to get the latest currencies exchange rate
     * without parameters.
     *
     * @description <a href="https://freecurrencyapi.com/docs/latest"/>
     */
    @GetMapping(value = "/latest", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getUSDLatestCurrency();

    /**
     * Implementation of the GET method to get the historical currencies exchange rate on a date
     * with:
     *      parameter date: must have and in format yyyy-mm-dd.
     *      parameter base_currency: a currency with which to search for exchange rates.
     *      parameter currencies: a list of currency code separated by ",".
     *
     * @description <a href="https://freecurrencyapi.com/docs/historical"/>
     */
    @GetMapping(value = "/historical", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getHistoricalCurrency(@RequestParam("date") String date,
                                   @RequestParam("base_currency") String baseCurrency,
                                   @RequestParam("currencies") String currencies);

    /**
     * Implementation of the GET method to get the historical currencies exchange rate on a date
     * with:
     *      parameter date: must have and in format yyyy-mm-dd.
     *      parameter base_currency: a currency with which to search for exchange rates.
     *
     * @description <a href="https://freecurrencyapi.com/docs/historical"/>
     */
    @GetMapping(value = "/historical", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getHistoricalCurrency(@RequestParam("date") String date,
                                   @RequestParam("base_currency") String baseCurrency);

    /**
     * Implementation of the GET method to get the historical currencies exchange rate on a date
     * with:
     *      parameter date: must have and in format yyyy-mm-dd.
     *
     * @description <a href="https://freecurrencyapi.com/docs/historical"/>
     */
    @GetMapping(value = "/historical", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getUSDHistoricalCurrency(@RequestParam("date") String date);

    /**
     * Implementation of the GET method to get the historical currencies exchange rate on a date
     * with:
     *      parameter date: must have and in format yyyy-mm-dd.
     *      parameter currencies: a list of currency code separated by ",".
     *
     * @description <a href="https://freecurrencyapi.com/docs/historical"/>
     */
    @GetMapping(value = "/historical", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getUSDHistoricalCurrency(@RequestParam("date") String date,
                                      @RequestParam("currencies") String currencies);
}
