package com.springboot.FlomadAIplanner.service.freeCurrency.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Slf4j
public class FreeCurrencyConfig {
    @Value("${freeCurrency.api.key}")
    private String apiKey;

    @Bean("freeCurrencyRequestInterceptor")
    @Qualifier("freeCurrencyRequestInterceptor")
    public RequestInterceptor requestInterceptor() {
        log.info("Initializing request interceptor for Free Currency API");
        return requestTemplate -> {
            log.info("Using Free Currency API key");
            requestTemplate.header("apikey", apiKey);
        };
    }
}
