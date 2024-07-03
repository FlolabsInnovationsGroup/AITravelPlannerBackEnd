package com.springboot.FlomadAIplanner.controller.viator;

import com.springboot.FlomadAIplanner.dto.viator.AttractionsRequest;
import com.springboot.FlomadAIplanner.exceptions.ViatorAPIException;
import com.springboot.FlomadAIplanner.service.viator.ViatorAttractionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/viator")
public class ViatorAttractionsController {
    @Autowired
    private ViatorAttractionsService attractionsService;

    @PostMapping("/attractions")
    public ResponseEntity<String> getAttractions(@RequestHeader("Accept-Language") String acceptLang, @RequestBody AttractionsRequest request) throws ViatorAPIException {
        log.info("Retrieving attractions for destination: {}", request.getDestId());

        String response = attractionsService.getAttractions(acceptLang, request);
        log.info("Attractions retrieved");
        return ResponseEntity.ok(response);
    }
}
