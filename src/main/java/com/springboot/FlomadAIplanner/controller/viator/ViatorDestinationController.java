package com.springboot.FlomadAIplanner.controller.viator;

import com.springboot.FlomadAIplanner.service.viator.ViatorDestinationsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/viator")
public class ViatorDestinationController {
    @Autowired
    private ViatorDestinationsService destinationsService;

    @GetMapping("/get_destinations")
    public String getDestinations() {
        log.info("Retrieving destinations");
        return destinationsService.getDestinations();
    }
}
