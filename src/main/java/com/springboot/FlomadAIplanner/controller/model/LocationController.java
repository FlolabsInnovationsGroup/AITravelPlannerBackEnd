package com.springboot.FlomadAIplanner.controller.model;

import com.springboot.FlomadAIplanner.dto.LocationDTO;
import com.springboot.FlomadAIplanner.model.entities.Location;
import com.springboot.FlomadAIplanner.service.model.LocationService;
import com.springboot.FlomadAIplanner.service.socket.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    // Session-based locks to prevent race conditions in parallel requests
    private final java.util.concurrent.ConcurrentHashMap<String, Object> sessionLocks = new java.util.concurrent.ConcurrentHashMap<>();

    private final LocationService locationService;
    private final WebSocketService webSocketService;

    @PostMapping("/add")
    public ResponseEntity<?> addLocation(@Valid @RequestBody LocationDTO request) {
        try {
            // Validate sessionId explicitly
            if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Session ID is required", 
                                "message", "A valid session ID must be provided to add a location"));
            }
            
            // Use session-specific lock to prevent race conditions in parallel requests
            Object lock = sessionLocks.computeIfAbsent(request.getSessionId(), k -> new Object());
            
            synchronized (lock) {
                // ⚠️ GEOGRAPHIC VALIDATION: Check if location is too far from existing locations
                List<Location> existingLocations = locationService.findAllBySessionId(request.getSessionId());
                if (!existingLocations.isEmpty()) {
                    Location firstLocation = existingLocations.get(0);
                    double distanceToFirst = calculateHaversineDistance(
                        request.getLatitude(), request.getLongitude(),
                        firstLocation.getLatitude(), firstLocation.getLongitude()
                    );
                    
                    // Reject locations more than 500km from the first location (trip anchor)
                    if (distanceToFirst > 500000) { // 500km in meters
                        String errorMsg = String.format(
                            "⚠️ Location verification failed: '%s' at (%.4f, %.4f) is %dkm away from existing location '%s' at (%.4f, %.4f). " +
                            "Are you sure these locations are part of the same trip? " +
                            "If searching for '%s', make sure to include the destination region in your search query. " +
                            "For example: '%s, [destination city/country]' instead of just '%s'.",
                            request.getName(), request.getLatitude(), request.getLongitude(),
                            Math.round(distanceToFirst / 1000),
                            firstLocation.getName(), firstLocation.getLatitude(), firstLocation.getLongitude(),
                            request.getName(), request.getName(), request.getName()
                        );
                        
                        log.warn("⚠️ LOCATION VERIFICATION REJECTED: '{}' at ({}, {}) is {}km from '{}' at ({}, {}). This indicates wrong destination!",
                            request.getName(), request.getLatitude(), request.getLongitude(),
                            Math.round(distanceToFirst / 1000),
                            firstLocation.getName(), firstLocation.getLatitude(), firstLocation.getLongitude());
                        
                        return ResponseEntity.badRequest()
                            .body(Map.of("error", "Geographic validation failed", 
                                        "message", errorMsg,
                                        "distance_km", Math.round(distanceToFirst / 1000),
                                        "anchor_location", firstLocation.getName()));
                    }
                }
                
                // Step 1: Save location (passed validation)
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

                return ResponseEntity.ok(location);
            } // End synchronized block
        } catch (Exception e) {
            // Log the error for debugging
            log.error("Error adding location: {}", e.getMessage(), e);
            
            // Return detailed error information
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to add location", 
                            "message", e.getMessage(),
                            "details", "Please check that all required fields are provided and valid"));
        }
    }
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     * @return distance in meters
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371000; // meters
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }

    @PostMapping("/batch-add")
    public ResponseEntity<?> batchAddLocations(@RequestBody Map<String, Object> request) {
        try {
            // Extract sessionId and locations list
            String sessionId = (String) request.get("sessionId");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> locationsList = (List<Map<String, Object>>) request.get("locations");
            
            // Validate sessionId
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Session ID is required"));
            }
            
            if (locationsList == null || locationsList.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Locations list cannot be empty"));
            }
            
            log.info("Batch adding {} locations for session: {}", locationsList.size(), sessionId);
            
            List<Location> savedLocations = new java.util.ArrayList<>();
            List<Map<String, String>> errors = new java.util.ArrayList<>();
            
            for (int i = 0; i < locationsList.size(); i++) {
                Map<String, Object> locData = locationsList.get(i);
                try {
                    // Extract and validate coordinates
                    Object latObj = locData.get("latitude");
                    Object lngObj = locData.get("longitude");
                    
                    if (latObj == null || lngObj == null) {
                        errors.add(Map.of("index", String.valueOf(i), "error", "Missing coordinates"));
                        continue;
                    }
                    
                    Double latitude = latObj instanceof Number ? ((Number) latObj).doubleValue() : Double.parseDouble(latObj.toString());
                    Double longitude = lngObj instanceof Number ? ((Number) lngObj).doubleValue() : Double.parseDouble(lngObj.toString());
                    
                    // Build location
                    Location location = Location.builder()
                        .sessionId(sessionId)
                        .latitude(latitude)
                        .longitude(longitude)
                        .name((String) locData.getOrDefault("name", "Unnamed"))
                        .type((String) locData.getOrDefault("type", "attraction"))
                        .build();
                    
                    // Save location
                    location = locationService.save(location);
                    savedLocations.add(location);
                    
                    // Send WebSocket update
                    webSocketService.sendLocationUpdate(location);
                    
                } catch (Exception e) {
                    errors.add(Map.of(
                        "index", String.valueOf(i),
                        "name", (String) locData.getOrDefault("name", "Unknown"),
                        "error", e.getMessage()
                    ));
                    log.warn("Failed to add location at index {}: {}", i, e.getMessage());
                }
            }
            
            // Return summary
            Map<String, Object> response = new HashMap<>();
            response.put("success", savedLocations.size());
            response.put("failed", errors.size());
            response.put("total", locationsList.size());
            response.put("locations", savedLocations);
            if (!errors.isEmpty()) {
                response.put("errors", errors);
            }
            
            log.info("Batch add complete: {} success, {} failed", savedLocations.size(), errors.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in batch add: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Batch add failed", "message", e.getMessage()));
        }
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<List<Location>> getLocationsBySession(@PathVariable String sessionId) {
        try {
            // Validate session ID parameter
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            
            List<Location> locations = locationService.findAllBySessionId(sessionId);
            // Return empty list instead of null for new sessions
            if (locations == null) {
                locations = List.of(); // Empty list
            }
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            // Log the error for debugging but return empty list for better UX
            System.err.println("Error fetching locations for session " + sessionId + ": " + e.getMessage());
            // Return empty list instead of 500 error to avoid breaking the UI
            return ResponseEntity.ok(List.of());
        }
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> removeLocation(@PathVariable String id) {
        try {
            Location location = locationService.findById(id);
            if (location == null) {
                return ResponseEntity.notFound().build();
            }
            String sessionId = location.getSessionId();
            locationService.delete(id);
            webSocketService.sendLocationRemoved(sessionId, id);
            return ResponseEntity.ok("Location removed successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error removing location: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear/{sessionId}")
    public ResponseEntity<String> clearLocationsBySession(@PathVariable String sessionId) {
        try {
            // Validate session ID parameter
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid session ID");
            }
            
            locationService.deleteAllBySessionId(sessionId);

            // Notify clients that the map has been cleared
            webSocketService.sendMapCleared(sessionId);

            return ResponseEntity.ok("All locations cleared for session ID: " + sessionId);
        } catch (Exception e) {
            System.err.println("Error clearing locations for session " + sessionId + ": " + e.getMessage());
            // Still return success even if clearing failed (empty session case)
            return ResponseEntity.ok("Session cleared (no locations found): " + sessionId);
        }
    }

    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        errors.put("error", "Validation failed");
        errors.put("message", "Please check the provided data");
        errors.put("fieldErrors", fieldErrors);
        
        return ResponseEntity.badRequest().body(errors);
    }
}
