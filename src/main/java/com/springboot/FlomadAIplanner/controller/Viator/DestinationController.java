package com.springboot.FlomadAIplanner.controller.Viator;

import com.springboot.FlomadAIplanner.service.Viator.DestinationsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/viator")
public class DestinationController {
    @Autowired
    private DestinationsService destinationsService;

    @GetMapping("/get_destinations")
    public ResponseEntity<String> getDestinations() {
        log.info("Retrieving destinations");
        return destinationsService.getDestinations();
    }
}
