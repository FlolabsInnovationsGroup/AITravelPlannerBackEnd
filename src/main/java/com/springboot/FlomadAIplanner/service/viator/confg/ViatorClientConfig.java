package com.springboot.FlomadAIplanner.service.viator.confg;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ViatorClientConfig {

    @Value("${viator.api.partnerKey}")
    private String apiKey;

    @Bean("viatorRequestInterceptor")
    public RequestInterceptor requestInterceptor() {
        log.info("Using request interceptor for Viator API");
        return requestTemplate -> requestTemplate.header("exp-api-key", apiKey);
    }
}
