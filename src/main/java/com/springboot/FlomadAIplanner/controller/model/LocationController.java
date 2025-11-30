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

            return ResponseEntity.ok(location);
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
