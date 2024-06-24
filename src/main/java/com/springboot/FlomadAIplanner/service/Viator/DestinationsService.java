package com.springboot.FlomadAIplanner.service.Viator;

import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
// import org.apache.http.HttpHeaders;



@Service
public class DestinationsService {
    private static final Logger logger = Logger.getLogger(DestinationsService.class.getName());
    @Value("${viator.api.partnerKey}")
    private String apiKey;

    @Value("${viator.api.url}")
    private String baseUrl;

    public ResponseEntity<String> getDestinations() {
        String url = baseUrl + "/v1/taxonomy/destinations";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("exp-api-key", apiKey);
        headers.set("Accept-Language","en-US");
        headers.set("Accept","application/json");
        
        headers.setContentType(MediaType.APPLICATION_JSON);
        logger.info("headers: " + headers);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        logger.info("entity: " + entity);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return response;
    }
}
