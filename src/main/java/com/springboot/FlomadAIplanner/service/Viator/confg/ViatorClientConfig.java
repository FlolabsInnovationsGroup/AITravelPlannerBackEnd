package com.springboot.FlomadAIplanner.service.Viator.confg;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class ViatorClientConfig {

    @Value("${viator.api.partnerKey}")
    private String apiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> requestTemplate.header("exp-api-key", apiKey);
    }
}
