package com.springboot.FlomadAIplanner.service.Viator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
// import org.apache.http.HttpHeaders;


@Slf4j
@Service
public class DestinationsService {
    @Value("${viator.api.partnerKey}")
    private String apiKey;

    @Value("${viator.api.url}")
    private String baseUrl;

    public ResponseEntity<String> getDestinations() {
        log.info("Getting destinations");
        String url = baseUrl + "/v1/taxonomy/destinations";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("exp-api-key", apiKey);
        headers.set("Accept-Language", "en-US");
        headers.set("Accept", "application/json");

        headers.setContentType(MediaType.APPLICATION_JSON);
        log.info("headers: " + headers);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        log.info("entity: " + entity);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return response;
    }
}
