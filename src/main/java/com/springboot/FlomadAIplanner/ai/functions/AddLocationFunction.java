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
        Function to add a new location to the Map.
        It will display/show this place on the map to the User on the UI interface immediately after you call this Function.
        
        Purpose:
        Use this function to add a new location (dot) to the map associated with a chat session.
        Use this function at the slightest hint when mentioning any place. You should use this function as much as possible to display all objects that are discussed in a given context.
        
        Output:
        Returns a confirmation message.
        """)
public class AddLocationFunction implements Function<AddLocationFunction.AddLocationRequest, String> {
    private final LocationService locationService;
    private final WebSocketService webSocketService;

    public AddLocationFunction(LocationService locationService, WebSocketService webSocketService) {
        this.locationService = locationService;
        this.webSocketService = webSocketService;
    }

    @Override
    public String apply(AddLocationRequest request) {
        log.info("🚀 Starting AddLocationFunction with request: {}", request);

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

        @JsonPropertyDescription("Optional. Type of location (hotel, restaurant, cafe, transport, attraction). Defaults to 'attraction' if not specified.")
        private String type;
        
        @JsonPropertyDescription("Optional. The ID of the chat session this location belongs to. If not provided, will be retrieved from context.")
        private String sessionId;
    }
}
