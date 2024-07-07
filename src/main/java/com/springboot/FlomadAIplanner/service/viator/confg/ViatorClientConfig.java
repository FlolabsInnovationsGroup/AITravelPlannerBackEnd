package com.springboot.FlomadAIplanner.service.viator.confg;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Slf4j
public class ViatorClientConfig {

    @Value("${viator.api.partnerKey}")
    private String apiKey;

    @Bean("viatorRequestInterceptor")
    @Qualifier("viatorRequestInterceptor")
    public RequestInterceptor requestInterceptor() {
        log.info("Using request interceptor for Viator API");
        return requestTemplate -> requestTemplate.header("exp-api-key", apiKey);
    }
}
