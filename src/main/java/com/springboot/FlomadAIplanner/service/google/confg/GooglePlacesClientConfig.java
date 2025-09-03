package com.springboot.FlomadAIplanner.service.google.confg;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * Configuration class for the Google Places API client.
 * <p>
 * This class provides configuration options for interacting with the Google Places API using Feign.
 * It manages the API key and injects it as a request header for proper API authentication.
 */
@Slf4j
public class GooglePlacesClientConfig {

    @Value("${google.api.key}")
    private String apiKey;

    /**
     * Creates a Feign request interceptor that adds the API key to request headers.
     * <p>
     * This interceptor ensures that all outgoing requests to the Google Places API include the
     * necessary API key for authentication.
     *
     * @return A Feign {@link RequestInterceptor} that adds the API key to headers.
     */
    @Bean("googleRequestInterceptor")
    @Qualifier("googleRequestInterceptor")
    public RequestInterceptor requestInterceptor() {
        log.info("Initializing request interceptor for Google Maps API");
        return requestTemplate -> {
            log.info("Using Google API key");
            requestTemplate.query("key", apiKey);
        };
    }
}
