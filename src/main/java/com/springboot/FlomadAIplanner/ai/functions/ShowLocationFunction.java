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

import java.util.function.Function;

@Slf4j
@Component
@Description("""
        MAGICAL LOCATION SPOTLIGHT: Show a single amazing location with beautiful focused map view! 
        Clears everything else and creates a stunning, clean display perfect for exploring one incredible destination.
        
        ✨ YOUR SPECIAL POWER: Single-location magic - clear the map and spotlight only the requested location
        
        🌟 PERFECT FOR THESE AMAZING MOMENTS:
        - Fresh location discovery: "Show me Paris", "Take me to Rome", "I want to see Tokyo"
        - Starting exciting new travel research
        - Creating clean, focused map views: "Focus on London only"
        - Single destination exploration adventures
        
        🎯 WHEN TO USE vs AddLocationFunction:
        ✅ Use ShowLocationFunction: "Show me Paris", "I want to see Tokyo", "Take me to Rome"
        ❌ Don't use for: Adding to existing locations like "Also show Rome" (use AddLocationFunction instead)
        
        🗺️ MAGICAL BEHAVIOR DIFFERENCE: 
        - ShowLocationFunction: CLEARS existing + shows new location (like a spotlight!)
        - AddLocationFunction: KEEPS existing + adds new location (like collecting gems!)
        
        ⚡ INCREDIBLE FEATURES:
        - Beautiful map clearing for perfect focused views
        - Instant real-time UI updates via WebSocket magic
        - Smart session context management
        - Immediate visual feedback that delights users
        
        🎪 USAGE MAGIC RULE: One location per call - make each spotlight moment special!
        
        Returns: Enthusiastic, varied confirmation messages that celebrate the location display
        """)
public class ShowLocationFunction implements Function<ShowLocationFunction.ShowLocationRequest, String> {
    private final LocationService locationService;
    private final WebSocketService webSocketService;

    public ShowLocationFunction(LocationService locationService, WebSocketService webSocketService) {
        this.locationService = locationService;
        this.webSocketService = webSocketService;
    }

    @Override
    public String apply(ShowLocationRequest request) {
        log.info("🎯 Starting ShowLocationFunction with request: {}", request);

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
                log.error("❌ Session ID is missing in both request and context. Cannot proceed with showing location.");
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

            // STEP 1: Check existing locations for this session
            log.info("🔍 Checking existing locations for session: {}", sessionId);
            var existingLocations = locationService.findAllBySessionId(sessionId);
            
            // Check if the requested location is already the only location displayed
            // Compare both name AND coordinates to ensure it's truly the same location
            boolean isAlreadyShowing = false;
            if (existingLocations.size() == 1) {
                Location existingLocation = existingLocations.get(0);
                boolean sameLocation = existingLocation.getName().equalsIgnoreCase(request.getName()) &&
                    Math.abs(existingLocation.getLatitude() - request.getLatitude()) < 0.0001 &&
                    Math.abs(existingLocation.getLongitude() - request.getLongitude()) < 0.0001;
                isAlreadyShowing = sameLocation;
            }
                
            if (isAlreadyShowing) {
                log.info("✅ Location '{}' is already showing as the only location. No changes needed.", request.getName());
                return generateVariedResponse(request.getName(), "already_showing");
            }
            
            // STEP 2: Clear existing locations for this session
            log.info("🧹 Clearing existing locations for session: {}", sessionId);
            locationService.deleteAllBySessionId(sessionId);
            webSocketService.sendMapCleared(sessionId);

            // STEP 3: Add the new location
            Location location = Location.builder()
                    .sessionId(sessionId)
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .name(request.getName())
                    .type(request.getType() != null ? request.getType() : "attraction")
                    .build();

            // Save the new location to the database
            locationService.save(location);
            log.info("📍 New location saved: {}", location);

            // Notify the frontend via WebSocket
            webSocketService.sendLocationUpdate(location);
            log.info("📡 WebSocket update sent for location: {}", location.getName());

            return generateVariedResponse(location.getName(), "new_location");
        } catch (Exception e) {
            log.error("❌ Error in ShowLocationFunction: {}", e.getMessage(), e);
            return "Error showing location: " + e.getMessage();
        }
    }

    /**
     * Generate varied, conversational responses instead of the same technical message
     */
    private String generateVariedResponse(String locationName, String scenario) {
        String[] alreadyShowingResponses = {
            "Perfect! " + locationName + " is already beautifully displayed on your map! ✨",
            "You've got " + locationName + " right where you want it - looking great on the map! 🗺️",
            locationName + " is already taking center stage on your map - what a fantastic choice! 🌟"
        };
        
        String[] newLocationResponses = {
            "✨ Beautiful! I've placed " + locationName + " perfectly on your map - take a look!",
            "🗺️ There we go! " + locationName + " is now beautifully displayed and ready to explore!",
            "Perfect! " + locationName + " is now shining bright on your map - what an amazing destination! ⭐",
            "🎯 Wonderful! I've focused your map on " + locationName + " - you're going to love exploring this spot!",
            "✅ Fantastic choice! " + locationName + " is now perfectly positioned on your map for exploration!"
        };
        
        if ("already_showing".equals(scenario)) {
            int index = (int) (Math.random() * alreadyShowingResponses.length);
            return alreadyShowingResponses[index];
        } else {
            int index = (int) (Math.random() * newLocationResponses.length);
            return newLocationResponses[index];
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonClassDescription("Request to show a specific location by clearing the map and displaying only this location. JSON Schema of type: 'object'")
    public static class ShowLocationRequest {

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
