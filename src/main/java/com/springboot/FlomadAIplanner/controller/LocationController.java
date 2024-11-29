package com.springboot.FlomadAIplanner.controller;

import com.springboot.FlomadAIplanner.ai.functions.AddLocationFunction.AddLocationRequest;
import com.springboot.FlomadAIplanner.model.entities.Location;
import com.springboot.FlomadAIplanner.service.model.LocationService;
import com.springboot.FlomadAIplanner.service.socket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final WebSocketService webSocketService;

    @PostMapping("/add")
    public ResponseEntity<String> addLocation(@RequestBody AddLocationRequest request) {
        try {
            // Step 1: Save location
            Location location = Location.builder()
                    .sessionId(request.getSessionId())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .name(request.getName())
                    .type(request.getType())
                    .build();
            location = locationService.save(location);

            // Step 2: Notify clients via WebSocket
            webSocketService.sendLocationUpdate(location);

            return ResponseEntity.ok("Location added successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error adding location: " + e.getMessage());
        }
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<List<Location>> getLocationsBySession(@PathVariable String sessionId) {
        try {
            List<Location> locations = locationService.findAllBySessionId(sessionId);
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> removeLocation(@PathVariable String id) {
        try {
            locationService.delete(id);
            return ResponseEntity.ok("Location removed successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error removing location: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear/{sessionId}")
    public ResponseEntity<String> clearLocationsBySession(@PathVariable String sessionId) {
        try {
            locationService.deleteAllBySessionId(sessionId);

            // Notify clients that the map has been cleared
            webSocketService.sendMapCleared(sessionId);

            return ResponseEntity.ok("All locations cleared for session ID: " + sessionId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error clearing locations: " + e.getMessage());
        }
    }
}
