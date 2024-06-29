package com.springboot.FlomadAIplanner.controller.viator;

import com.springboot.FlomadAIplanner.DTO.AttractionsRequest;
import com.springboot.FlomadAIplanner.service.viator.ViatorAttractionsService;
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
public class ViatorAttractionsController {
    @Autowired
    private ViatorAttractionsService attractionsService;

    @PostMapping("/attractions")
    public ResponseEntity<String> getAttractions(@RequestBody AttractionsRequest request) {
        log.info("Retrieving attractions for destination: {}", request.getDestId());

        String response = attractionsService.getAttractions(request.getDestId(), request.getTopX(), request.getSortOrder());
        log.info("Attractions retrieved");
        return ResponseEntity.ok(response);
    }
}
