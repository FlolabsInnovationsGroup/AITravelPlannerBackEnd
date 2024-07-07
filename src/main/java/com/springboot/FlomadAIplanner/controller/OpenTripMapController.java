package com.springboot.FlomadAIplanner.controller;

import com.springboot.FlomadAIplanner.service.OpenTripMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/opentripmap")
public class OpenTripMapController {
    private final OpenTripMapService openTripMapService;

    @Autowired
    public OpenTripMapController(OpenTripMapService openTripMapService) {
        this.openTripMapService = openTripMapService;
    }

    @GetMapping("/places")
    public ResponseEntity<String> getPlaces(@RequestParam String lon, @RequestParam String lat, @RequestParam String radius) {
        return ResponseEntity.ok(openTripMapService.getPlaces(lon, lat, radius));
    }

    @GetMapping("/place/{xid}")
    public ResponseEntity<String> getPlaceDetails(@RequestParam String xid) {
        return ResponseEntity.ok(openTripMapService.getPlaceDetails(xid));
    }
}