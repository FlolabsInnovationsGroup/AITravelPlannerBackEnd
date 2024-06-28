package com.springboot.FlomadAIplanner.controller.Viator;

import com.springboot.FlomadAIplanner.DTO.AttractionsRequest;
import com.springboot.FlomadAIplanner.service.Viator.AttractionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/viator")
public class AttractionsController {
    @Autowired
    private AttractionsService attractionsService;

    @PostMapping("/attractions")
    public ResponseEntity<String> getAttractions(@RequestBody AttractionsRequest request) {
        log.info("Retrieving attractions for destination: {}", request.getDestId());
        try {
            String response = attractionsService.getAttractions(request.getDestId(), request.getTopX(), request.getSortOrder());
            log.info("Attractions retrieved");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("An error occurred while retrieving attractions: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
        }
    }
}
