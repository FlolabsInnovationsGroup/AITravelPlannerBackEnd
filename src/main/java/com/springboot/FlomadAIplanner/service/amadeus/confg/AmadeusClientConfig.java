package com.springboot.FlomadAIplanner.service.amadeus.confg;

import com.springboot.FlomadAIplanner.exceptions.AmadeusAPIException;
import com.springboot.FlomadAIplanner.service.amadeus.AmadeusAuthService;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AmadeusClientConfig {
    private final AmadeusAuthService amadeusAuthService;

    @Autowired
    public AmadeusClientConfig(AmadeusAuthService amadeusAuthService) {
        this.amadeusAuthService = amadeusAuthService;
    }

    @Bean("amadeusRequestInterceptor")
    public RequestInterceptor requestInterceptor() throws AmadeusAPIException {
        log.info("Using request interceptor for Amadeus API");
        String authorizationHeader = "Bearer " + amadeusAuthService.getAccessToken();
        return requestTemplate -> requestTemplate.header("Authorization", authorizationHeader);
    }

}
