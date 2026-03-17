package com.springboot.FlomadAIplanner.service.amadeus.confg;

import com.springboot.FlomadAIplanner.exceptions.api.AmadeusAPIException;
import com.springboot.FlomadAIplanner.service.amadeus.AmadeusAuthService;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

@Slf4j
public final class AmadeusClientConfig {

    /** Amadeus auth service for access tokens. */
    private final AmadeusAuthService amadeusAuthService;

    /**
     * Constructor.
     *
     * @param pAmadeusAuthService Amadeus auth service
     */
    public AmadeusClientConfig(final AmadeusAuthService pAmadeusAuthService) {
        this.amadeusAuthService = pAmadeusAuthService;
    }

    /**
     * Request interceptor bean that adds Bearer token to Amadeus API requests.
     *
     * @return request interceptor
     */
    @Bean("amadeusRequestInterceptor")
    @Qualifier("amadeusRequestInterceptor")
    public RequestInterceptor requestInterceptor() {
        log.info("Initializing request interceptor for Amadeus API");

        return requestTemplate -> {
            // Fetch the access token dynamically for each request
            String accessToken;
            try {
                accessToken = amadeusAuthService.getAccessToken();
            } catch (AmadeusAPIException e) {
                throw new RuntimeException(e);
            }
            String authorizationHeader = "Bearer " + accessToken;
            requestTemplate.header("Authorization", authorizationHeader);
            log.debug("Set Authorization header with dynamic access token");
        };
    }

}
