package com.springboot.FlomadAIplanner.controller.Amadeus;

import com.springboot.FlomadAIplanner.service.Amadeus.AmadeusAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/amadeus")
public class AmadeusAuthController {

    private final AmadeusAuthService amadeusAuthService;

    @Autowired
    public AmadeusAuthController(AmadeusAuthService amadeusAuthService) {
        this.amadeusAuthService = amadeusAuthService;
    }

    @GetMapping("/auth/token")
    public ResponseEntity<String> getAuthToken() {
        log.info("Retrieving access token");
        String token = amadeusAuthService.getAccessToken();
        if (token != null) {
            log.info("Access token retrieved");
            return ResponseEntity.ok(token);
        } else {
            log.info("Failed to retrieve access token");
            return ResponseEntity.badRequest().body("Failed to retrieve access token");
        }
    }
}