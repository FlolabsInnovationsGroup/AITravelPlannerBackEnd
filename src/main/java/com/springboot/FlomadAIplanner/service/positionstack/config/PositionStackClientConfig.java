package com.springboot.FlomadAIplanner.service.positionstack.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the PositionStack API Feign client.
 * Automatically injects the 'access_key' into each request.
 */
@Slf4j
@Configuration
public class PositionStackClientConfig {

    @Value("${position-stack.api.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor positionStackRequestInterceptor() {
        log.info("Initializing PositionStack request interceptor...");
        return requestTemplate -> {
            // Add the 'access_key' query parameter automatically
            requestTemplate.query("access_key", apiKey);
        };
    }
}
