package com.springboot.FlomadAIplanner.controller.DataPersistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.FlomadAIplanner.DatabaseManagement.Destinations;
import com.springboot.FlomadAIplanner.service.DataPersistance.DestinationsDatabaseService;

@RestController
public class DestinationDatabaseController {
    @Autowired
    private DestinationsDatabaseService destinationService;

    @GetMapping("/getDestinationId")
    public Integer getDestinationId(@RequestParam String destinationName) {
        return destinationService.getDestinationIdByName(destinationName);
    }

    @GetMapping("/countDestinations")
    public long countDestinations() {
        return destinationService.countDestinations();
    }

    @GetMapping("/getFirstDestination")
    public Destinations getFirstDestination() {
        return destinationService.getFirstDestination();
    }
}