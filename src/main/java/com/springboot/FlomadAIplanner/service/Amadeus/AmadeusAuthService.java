package com.springboot.FlomadAIplanner.service.Amadeus;

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


/**
 * @class
 * @description get Amadeus Auth token, without the token, Amadeus rejects the request
 */
@Slf4j
@Service
public class AmadeusAuthService {
    private final RestTemplate restTemplate;
    @Value("${amadeus.api.key}")
    private String clientId;
    @Value("${amadeus.api.secret}")
    private String clientSecret;

    public AmadeusAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAccessToken() {
        log.info("Getting access token from Amadeus");
        String url = "https://test.api.amadeus.com/v1/security/oauth2/token";

        // request head
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // request body
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("grant_type", "client_credentials");

        // HttpEntity object
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        log.info("Requesting access token from Amadeus");
        // send post request
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, requestEntity, Map.class);

        // get access_token
        if (responseEntity.getBody() != null && responseEntity.getBody().containsKey("access_token")) {
            log.info("Received access token from Amadeus");
            return responseEntity.getBody().get("access_token").toString();
        } else {
            log.error("Failed to get access token from Amadeus");
            return null;
        }
    }

}
