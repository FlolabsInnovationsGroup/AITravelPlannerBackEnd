package com.springboot.FlomadAIplanner.controller.Viator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.FlomadAIplanner.service.Viator.TourlistService;

@RestController
public class TourlistController {
    @Autowired
    private TourlistService tourlistService;

    @GetMapping("/tours")
    public String getTours(@RequestParam String destId, @RequestParam int topX) {
        return tourlistService.getTourList(destId, topX);
    }
}
