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
import java.util.function.Function;

@Slf4j
@Component
@Description("""
        Function to SHOW/FOCUS on a specific location by clearing the map and displaying only this location.
        This is for when users want to focus on a single place.
        
        Purpose:
        Use this function to clear existing locations and show only the requested location.
        Use this when the user wants to focus on a single destination.
        
        IMPORTANT USAGE GUIDELINES:
        - Use for "Show me Paris", "Take me to Rome", "I want to see Tokyo"
        - Use when user requests a single location without mentioning adding to existing ones
        - This will CLEAR existing locations and show only the new one
        - DO NOT call this function multiple times for the same user request
        
        Output:
        Returns a confirmation message.
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

            // STEP 1: Clear existing locations for this session
            log.info("🧹 Clearing existing locations for session: {}", sessionId);
            locationService.deleteAllBySessionId(sessionId);
            webSocketService.sendMapCleared(sessionId);

            // STEP 2: Add the new location
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

            return "✅ Now showing: " + location.getName();
        } catch (Exception e) {
            log.error("❌ Error in ShowLocationFunction: {}", e.getMessage(), e);
            return "Error showing location: " + e.getMessage();
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

        @JsonPropertyDescription("Optional. Type of location (hotel, restaurant, cafe, transport, attraction). Defaults to 'attraction' if not specified.")
        private String type;
        
        @JsonPropertyDescription("Optional. The ID of the chat session this location belongs to. If not provided, will be retrieved from context.")
        private String sessionId;
    }
}
