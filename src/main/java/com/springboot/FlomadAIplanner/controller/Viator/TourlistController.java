package com.springboot.FlomadAIplanner.controller.Viator;

import com.springboot.FlomadAIplanner.service.Viator.TourlistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TourlistController {
    @Autowired
    private TourlistService tourlistService;

    @GetMapping("/tours")
    public String getTours(@RequestParam String destId, @RequestParam int topX) {
        log.info("Retrieving tours for destination: {}", destId);
        return tourlistService.getTourList(destId, topX);
    }
}
