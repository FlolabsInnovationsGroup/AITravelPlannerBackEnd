package com.springboot.FlomadAIplanner.service.Viator;

import java.util.logging.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class AttractionsService {
    private static final Logger logger = Logger.getLogger(AttractionsService.class.getName());
    @Value("${viator.api.baseurl}")
    private String baseUrl; 

    @Value("${viator.api.partnerKey}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public AttractionsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAttractions(int destId, String topX, String sortOrder) {
        String url = baseUrl + "/v1/taxonomy/attractions";
        HttpHeaders headers = new HttpHeaders();
        headers.set("exp-api-key", apiKey);
        headers.set("Accept-Language", "en-US");
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBody = new JSONObject();
        requestBody.put("destId",destId);
        requestBody.put("topX", topX);
        requestBody.put("sortOrder", "RECOMMENDED");

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        logger.info("entity: " + entity);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        return response.getBody();

    }
}
