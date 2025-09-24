package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.model.entities.Location;
import com.springboot.FlomadAIplanner.service.model.LocationService;
import com.springboot.FlomadAIplanner.service.socket.WebSocketService;
import com.springboot.FlomadAIplanner.util.SessionContextHolder;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
@Component
@Description("""
        Adds locations to existing map display, building cumulative travel itineraries. Each location appears 
        as a visual marker on the user interface immediately after calling this function.
        
        CORE PURPOSE: Accumulative location building - adds to existing map without clearing previous locations
        
        PERFECT FOR:
        - Multi-destination itineraries: "Add Paris, then Rome, then London"
        - Progressive trip planning: "Also include Barcelona" 
        - Building comprehensive travel routes
        - Accumulating must-visit destinations
        
        WHEN TO USE vs ShowLocationFunction:
        ✅ Use AddLocationFunction: "Add Paris to my itinerary", "Also show Rome", "Include London too"
        ❌ Don't use for: Single location requests like "Show me Paris" (use ShowLocationFunction)
        
        SMART FEATURES:
        - Duplicate request prevention (30-second timeout)
        - Real-time UI updates via WebSocket
        - Session context awareness
        - Immediate visual feedback to users
        
        USAGE RULE: Do NOT call multiple times for same user request - consolidate locations into single call
        
        Returns: Confirmation message of successful location addition to map
        """)
public class AddLocationFunction implements Function<AddLocationFunction.AddLocationRequest, String> {
    private final LocationService locationService;
    private final WebSocketService webSocketService;
    
    // Duplicate request prevention (keep requests for 30 seconds)
    private static final ConcurrentHashMap<String, Long> processedRequests = new ConcurrentHashMap<>();
    private static final long DUPLICATE_TIMEOUT = 30000; // 30 seconds

    public AddLocationFunction(LocationService locationService, WebSocketService webSocketService) {
        this.locationService = locationService;
        this.webSocketService = webSocketService;
    }

    @Override
    public String apply(AddLocationRequest request) {
        log.info("🚀 Starting AddLocationFunction with request: {}", request);

        // Prevent duplicate function calls by checking if this exact request was processed recently
        String requestHash = generateRequestHash(request);
        if (isDuplicateRequest(requestHash)) {
            log.warn("⚠️ Duplicate AddLocationFunction call detected, skipping");
            return "✅ Location already processed: " + request.getName();
        }
        markRequestProcessed(requestHash);

        try {
            // Get sessionId from request first, then fall back to context
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = SessionContextHolder.getSessionId();
                log.info("🔍 Using sessionId from context: {}", sessionId);
            } else {
                log.info("🔍 Using sessionId from request: {}", sessionId);
            }

            if (sessionId == null || sessionId.isEmpty()) {
                log.error("❌ Session ID is missing in both request and context. Cannot proceed with adding location.");
                return "Error: Missing session ID in request and context.";
            }

            // Validate coordinates
            if (request.getLatitude() == null || request.getLongitude() == null) {
                log.error("❌ Invalid coordinates: lat={}, lng={}", request.getLatitude(), request.getLongitude());
                return "Error: Latitude and longitude are required.";
            }

            if (request.getLatitude() < -90 || request.getLatitude() > 90) {
                log.error("❌ Invalid latitude: {}", request.getLatitude());
                return "Error: Latitude must be between -90 and 90.";
            }

            if (request.getLongitude() < -180 || request.getLongitude() > 180) {
                log.error("❌ Invalid longitude: {}", request.getLongitude());
                return "Error: Longitude must be between -180 and 180.";
            }

            // Check for duplicate locations within 100 meters
            List<Location> existingLocations = locationService.findAllBySessionId(sessionId);
            for (Location existing : existingLocations) {
                double distance = calculateHaversineDistance(
                    request.getLatitude(), request.getLongitude(),
                    existing.getLatitude(), existing.getLongitude()
                );
                if (distance < 100) { // 100 meters threshold
                    log.warn("⚠️ Location too close to existing location '{}' ({}m away)", existing.getName(), Math.round(distance));
                    return String.format("Location is too close to existing location '%s' (%.0fm away). " +
                        "Locations must be at least 100 meters apart.", existing.getName(), distance);
                }
            }

            Location location = Location.builder()
                    .sessionId(sessionId)
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .name(request.getName())
                    .type(request.getType() != null ? request.getType() : "attraction")
                    .build();

            // Save the location to the database
            locationService.save(location);
            log.info("📍 Location saved: {}", location);

            // Notify the frontend via WebSocket
            webSocketService.sendLocationUpdate(location);
            log.info("📡 WebSocket update sent for location: {}", location.getName());

            return "✅ Location added successfully: " + location.getName();
        } catch (Exception e) {
            log.error("❌ Error in AddLocationFunction: {}", e.getMessage(), e);
            return "Error adding location: " + e.getMessage();
        }
    }

    /**
     * Calculate the distance between two coordinates using the Haversine formula
     * @param lat1 Latitude of first point
     * @param lng1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lng2 Longitude of second point
     * @return Distance in meters
     */
    private double calculateHaversineDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371000; // Earth's radius in meters
        final double φ1 = Math.toRadians(lat1);
        final double φ2 = Math.toRadians(lat2);
        final double Δφ = Math.toRadians(lat2 - lat1);
        final double Δλ = Math.toRadians(lng2 - lng1);

        final double a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
                Math.cos(φ1) * Math.cos(φ2) *
                Math.sin(Δλ/2) * Math.sin(Δλ/2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c; // Distance in meters
    }

    /**
     * Generate a hash for duplicate detection
     */
    private String generateRequestHash(AddLocationRequest request) {
        return String.format("%s_%f_%f_%s", 
            SessionContextHolder.getSessionId(),
            request.getLatitude(), 
            request.getLongitude(),
            request.getName() != null ? request.getName() : "unnamed");
    }

    /**
     * Check if this request was processed recently
     */
    private boolean isDuplicateRequest(String requestHash) {
        cleanupOldRequests();
        Long lastProcessed = processedRequests.get(requestHash);
        if (lastProcessed != null) {
            return (System.currentTimeMillis() - lastProcessed) < DUPLICATE_TIMEOUT;
        }
        return false;
    }

    /**
     * Mark a request as processed
     */
    private void markRequestProcessed(String requestHash) {
        processedRequests.put(requestHash, System.currentTimeMillis());
    }

    /**
     * Clean up old processed requests to prevent memory leak
     */
    private void cleanupOldRequests() {
        long currentTime = System.currentTimeMillis();
        processedRequests.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > DUPLICATE_TIMEOUT);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonClassDescription("Request to add a new location to the map. JSON Schema of type: 'object'")
    public static class AddLocationRequest {

        @NotNull(message = "Latitude cannot be null.")
        @JsonPropertyDescription("Required. Latitude of the location.")
        private Double latitude;

        @NotNull(message = "Longitude cannot be null.")
        @JsonPropertyDescription("Required. Longitude of the location.")
        private Double longitude;

        @JsonPropertyDescription("Optional. Name or description of the location.")
        private String name;

        @JsonPropertyDescription("Optional. Type of location (attraction, cafe, hotel, restaurant, transport, park, museum, beach, mountain, landmark, shopping, entertainment, religious, hospital, airport, station, viewpoint, nature, sports, accommodation). Defaults to 'attraction' if not specified.")
        private String type;
        
        @JsonPropertyDescription("Optional. The ID of the chat session this location belongs to. If not provided, will be retrieved from context.")
        private String sessionId;
    }
}
