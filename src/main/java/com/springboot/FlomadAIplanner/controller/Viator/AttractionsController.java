package com.springboot.FlomadAIplanner.controller.Viator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.FlomadAIplanner.service.Viator.AttractionsService;

@RestController
@RequestMapping("/api/viator")
public class AttractionsController {
    @Autowired
    private AttractionsService attractionsService;

    @PostMapping("/attractions")
    public ResponseEntity<String> getAttractions(@RequestBody AttractionsRequest request) {
        try {
            String response = attractionsService.getAttractions(request.getDestId(), request.getTopX(), request.getSortOrder());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
        }
    }
}
