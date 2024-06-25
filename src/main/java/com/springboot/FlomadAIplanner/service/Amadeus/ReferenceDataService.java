package com.springboot.FlomadAIplanner.service.Amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class ReferenceDataService {


    private AmadeusAuthService amadeusAuthService = null;

    @Autowired
    public ReferenceDataService(AmadeusAuthService amadeusAuthService) {
        this.amadeusAuthService = amadeusAuthService;
    }


    @Value("${amadeus.api.key}")
    private String apiKey;

    @Value("${amadeus.api.secret}")
    private String apiSecret;

    public JsonNode getAirportCode(String cityName) {
        log.info("Getting airport code for city: {}", cityName);
        final String url = "https://test.api.amadeus.com/v1/reference-data/locations?keyword=" + cityName + "&subType=CITY,AIRPORT";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        String accessToken = amadeusAuthService.getAccessToken();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        log.info("Sending request to Amadeus");
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
        log.info("Received response from Amadeus: {}", response.getBody());
        return response.getBody();
    }
}
