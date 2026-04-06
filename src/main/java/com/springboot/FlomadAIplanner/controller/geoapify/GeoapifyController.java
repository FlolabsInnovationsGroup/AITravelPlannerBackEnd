package com.springboot.FlomadAIplanner.controller.geoapify;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.service.geoapify.GeoapifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/geoapify")
public class GeoapifyController {

    private final GeoapifyService geoapifyService;

    public GeoapifyController(GeoapifyService geoapifyService) {
        this.geoapifyService = geoapifyService;
    }

    @GetMapping("/geocode")
    public ResponseEntity<JsonNode> geocode(@RequestParam String text) {
        log.info("Geocode request: {}", text);
        return ResponseEntity.ok(geoapifyService.geocode(text));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<JsonNode> autocomplete(
            @RequestParam String text,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {
        log.info("Autocomplete request: {}", text);
        return ResponseEntity.ok(geoapifyService.autocomplete(text, lat, lon));
    }

    @GetMapping("/places")
    public ResponseEntity<JsonNode> searchPlaces(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "1000") double radius,
            @RequestParam(required = false) String categories) {
        log.info("Places search: {},{} radius={} categories={}", lat, lon, radius, categories);
        return ResponseEntity.ok(geoapifyService.searchNearbyPlaces(lat, lon, radius, categories));
    }

    @GetMapping("/directions")
    public ResponseEntity<JsonNode> getDirections(
            @RequestParam String waypoints,
            @RequestParam(defaultValue = "drive") String mode) {
        log.info("Directions request: waypoints={} mode={}", waypoints, mode);
        return ResponseEntity.ok(geoapifyService.getDirections(waypoints, mode));
    }
}
