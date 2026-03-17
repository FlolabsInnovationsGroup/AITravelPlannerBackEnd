package com.springboot.FlomadAIplanner.service.viator.confg;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Slf4j
public final class ViatorClientConfig {

    /** Viator API partner key. */
    @Value("${viator.api.partnerKey}")
    private String apiKey;

    /**
     * Feign request interceptor that adds API key header.
     *
     * @return request interceptor
     */
    @Bean("viatorRequestInterceptor")
    @Qualifier("viatorRequestInterceptor")
    public RequestInterceptor requestInterceptor() {
        log.info("Using request interceptor for Viator API");
        return requestTemplate -> requestTemplate.header("exp-api-key", apiKey);
    }
}
