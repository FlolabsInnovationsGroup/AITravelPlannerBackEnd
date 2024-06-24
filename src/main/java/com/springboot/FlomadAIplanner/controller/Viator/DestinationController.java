package com.springboot.FlomadAIplanner.controller.Viator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.FlomadAIplanner.service.Viator.DestinationsService;

@RestController
@RequestMapping("/api/viator")
public class DestinationController {
    @Autowired
    private DestinationsService destinationsService;

    @GetMapping("/get_destinations")
    public ResponseEntity<String> getDestinations() {
        return destinationsService.getDestinations();
    }
}
