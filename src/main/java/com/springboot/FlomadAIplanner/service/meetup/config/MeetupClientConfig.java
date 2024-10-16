package com.springboot.FlomadAIplanner.service.meetup.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Component;

@Slf4j
public class MeetupClientConfig {

/**    @Value("${meetup.api.key}")
    private String apiKey;

    /**
     * Creates a Feign request interceptor that adds the API key to request headers.
     *
     * @return A Feign {@link RequestInterceptor} that adds the API key to headers.
     */
/**    @Bean("meetupRequestInterceptor")
    @Qualifier("meetupRequestInterceptor")
    public RequestInterceptor requestInterceptor() {
        log.info("Initializing request interceptor for Meetup API");
        return requestTemplate -> {
            log.info("Using Meetup API key");
            requestTemplate.header("Authorization", "Bearer " + apiKey);
        };
    }
}**/


    private final OAuth2AuthorizedClientService authorizedClientService;

    public MeetupClientConfig(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @Bean
    public RequestInterceptor oauth2RequestInterceptor(@RegisteredOAuth2AuthorizedClient("meetup") OAuth2AuthorizedClient authorizedClient) {
        return requestTemplate -> {
            String token = authorizedClient.getAccessToken().getTokenValue();
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }
}
