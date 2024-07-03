package com.springboot.FlomadAIplanner.service.amadeus;

import com.springboot.FlomadAIplanner.exceptions.AmadeusAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class AmadeusAuthService {

    private final RestTemplate restTemplate;

    @Value("${amadeus.api.oauth2.url}")
    private String amadeusOauthApiUrl;

    @Value("${amadeus.api.key}")
    private String clientId;

    @Value("${amadeus.api.secret}")
    private String clientSecret;

    public AmadeusAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAccessToken() throws AmadeusAPIException {
        log.info("Getting access token from Amadeus");
        return retrieveAccessToken();
    }

    private String retrieveAccessToken() throws AmadeusAPIException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("grant_type", "client_credentials");
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(amadeusOauthApiUrl, requestEntity, Map.class);
            Map<String, Object> responseBody = responseEntity.getBody();
            if (responseBody != null && responseBody.containsKey("access_token")) {
                String accessToken = responseBody.get("access_token").toString();
                log.info("Received access token from Amadeus");
                return accessToken;
            } else {
                throw new AmadeusAPIException("Failed to get access token: Failed to authorize in third-party service.");
            }
        } catch (Exception e) {
            log.error("Failed to get access token from Amadeus", e);
            throw new AmadeusAPIException("Error retrieving access token from Amadeus", e);
        }
    }
}
