package com.springboot.FlomadAIplanner.controller.Amadeus;

import com.springboot.FlomadAIplanner.service.Amadeus.AmadeusAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

////////////////////////////////////////////

/**
 * @class
 * @description This is a controller that checking whether the price of flights you fetched have changed.
 */
//////////////////////////////////////////
@Slf4j
@RestController
public class PricingController {
    @Value("${amadeus.api.key}")
    private String apiKey;

    @Value("${amadeus.api.secret}")
    private String apiSecret;

    private final AmadeusAuthService amadeusAuthService;

    @Autowired
    public PricingController(AmadeusAuthService amadeusAuthService) {
        this.amadeusAuthService = amadeusAuthService;
    }

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/pricing")
    public String verifyFlightOffer(@RequestBody String offer) {
        try {
            log.info("Received offer: {}", offer);
            String accessToken = amadeusAuthService.getAccessToken();
            if (accessToken == null) {
                log.error("Failed to retrieve access token");
                return "Failed to retrieve access token";
            }
            log.info("Access Token Retrieved");

            String verifyUrl = "https://test.api.amadeus.com/v1/shopping/flight-offers/pricing";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Content-Type", "application/json");
            headers.set("X-HTTP-Method-Override", "GET");

            HttpEntity<String> request = new HttpEntity<>(offer, headers);
            System.out.println("Headers: " + request.getHeaders().toSingleValueMap());
            System.out.println("Body: " + request.getBody());

            log.info("Sending request to Amadeus API");
            ResponseEntity<String> response = restTemplate.postForEntity(verifyUrl, request, String.class);
            log.info("Received response from Amadeus API");
            return response.getBody();
        } catch (Exception e) {
            log.error("Error processing the request: {}", e.getMessage());
            e.printStackTrace();
            return "Error processing the request: " + e.getMessage();
        }
    }
}
