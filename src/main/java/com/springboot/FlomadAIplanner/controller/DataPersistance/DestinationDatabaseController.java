package com.springboot.FlomadAIplanner.controller.DataPersistance;

import com.springboot.FlomadAIplanner.model.entities.Destination;
import com.springboot.FlomadAIplanner.service.DataPersistance.DestinationsDatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class DestinationDatabaseController {
    @Autowired
    private DestinationsDatabaseService destinationService;

    @GetMapping("/getDestinationId")
    public Integer getDestinationId(@RequestParam String destinationName) {
        log.info("Retrieving destination id for destination: {}", destinationName);
        return destinationService.getDestinationIdByName(destinationName);
    }

    @GetMapping("/countDestinations")
    public long countDestinations() {
        log.info("Counting destinations");
        return destinationService.countDestinations();
    }

    @GetMapping("/getFirstDestination")
    public Destination getFirstDestination() {
        log.info("Retrieving first destination");
        return destinationService.getFirstDestination();
    }
}