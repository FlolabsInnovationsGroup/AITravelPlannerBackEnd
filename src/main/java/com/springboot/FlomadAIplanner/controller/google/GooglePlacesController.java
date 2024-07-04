package com.springboot.FlomadAIplanner.controller.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.exceptions.GoogleAPIException;
import com.springboot.FlomadAIplanner.service.google.GooglePlacesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/places")
public class GooglePlacesController {

    @Autowired
    private GooglePlacesService googlePlacesService;

    @PostMapping("/search")
    public JsonNode searchNearbyPlaces(@RequestBody NearbyPlacesRequest nearbyPlacesRequest) throws GoogleAPIException {
        return googlePlacesService.searchNearbyPlaces(nearbyPlacesRequest);
    }
}


