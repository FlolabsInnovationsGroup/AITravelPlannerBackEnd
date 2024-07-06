package com.springboot.FlomadAIplanner.service.google.confg;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the Google Places API client.
 *
 * This class provides configuration options for interacting with the Google Places API using Feign.
 * It manages the API key and injects it as a request header for proper API authentication.
 */
@Configuration
public class GooglePlacesClientConfig {

    /**
     * Creates a Feign request interceptor that adds the API key to request headers.
     *
     * This interceptor ensures that all outgoing requests to the Google Places API include the
     * necessary API key for authentication.
     *
     * @return A Feign {@link RequestInterceptor} that adds the API key to headers.
     */
    @Value("${google.api.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> requestTemplate.header("X-Goog-Api-Key", apiKey);
    }
}
