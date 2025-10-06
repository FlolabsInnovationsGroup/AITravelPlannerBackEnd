package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.model.entities.Location;
import com.springboot.FlomadAIplanner.service.model.LocationService;
import com.springboot.FlomadAIplanner.service.socket.WebSocketService;
import com.springboot.FlomadAIplanner.util.SessionContextHolder;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Component
@Description("""
        Function to remove a specific location by its name (e.g., "Eiffel Tower", "Times Square").
        
        Purpose:
        Use this function when a user asks to remove a location by name from the map.
        This function automatically handles the workflow of finding the location by name and removing it.
        
        Usage Examples:
        - "Remove the Eiffel Tower from the map"
        - "Delete Times Square"
        - "Take off the Louvre Museum"
        
        This function will:
        1. Search for locations with names that match or contain the provided name
        2. Remove the matching location from the map
        3. Send appropriate WebSocket notifications
        
        Output:
        Returns a confirmation message or error if the location is not found.
        """)
public class RemoveLocationByNameFunction implements Function<RemoveLocationByNameFunction.RemoveLocationByNameRequest, String> {

    private final LocationService locationService;
    private final WebSocketService webSocketService;

    public RemoveLocationByNameFunction(LocationService locationService, WebSocketService webSocketService) {
        this.locationService = locationService;
        this.webSocketService = webSocketService;
    }

    @Override
    public String apply(RemoveLocationByNameRequest request) {
        log.info("🔍 Attempting to remove location by name: '{}'", request.getLocationName());

        try {
            // Get session ID from context
            String sessionId = SessionContextHolder.getSessionId();
            log.info("📌 Session ID retrieved from context: {}", sessionId);

            if (sessionId == null || sessionId.isEmpty()) {
                log.error("❌ Failed to retrieve sessionId from context. RemoveLocationByNameFunction cannot proceed.");
                return "Error: Unable to determine session context.";
            }

            // Get all locations for the session
            List<Location> locations = locationService.findAllBySessionId(sessionId);
            log.info("📍 Found {} locations in session {}", locations.size(), sessionId);

            if (locations.isEmpty()) {
                log.warn("⚠️ No locations found in the current session.");
                return "I'm sorry, but there are no locations currently on the map to remove.";
            }

            // Search for location by name (case-insensitive, partial match)
            String searchName = request.getLocationName().toLowerCase().trim();
            Optional<Location> foundLocation = locations.stream()
                    .filter(location -> {
                        if (location.getName() == null) return false;
                        String locationName = location.getName().toLowerCase().trim();
                        // Match if the search name is contained in the location name or vice versa
                        return locationName.contains(searchName) || searchName.contains(locationName);
                    })
                    .findFirst();

            if (foundLocation.isEmpty()) {
                log.warn("⚠️ No location found with name containing: '{}'", request.getLocationName());
                log.info("📋 Available locations: {}", 
                    locations.stream()
                        .map(Location::getName)
                        .toList());
                return String.format("I'm sorry, but I couldn't find a location named '%s' on the map. " +
                    "Available locations are: %s", 
                    request.getLocationName(),
                    locations.stream().map(Location::getName).reduce((a, b) -> a + ", " + b).orElse("none"));
            }

            Location locationToRemove = foundLocation.get();
            String locationId = locationToRemove.getId();
            log.info("✅ Found location to remove: '{}' with ID: {}", locationToRemove.getName(), locationId);

            // Verify location exists before deleting
            if (!locationService.exists(locationId)) {
                log.warn("⚠️ Location ID '{}' not found in database!", locationId);
                return "Error: Location not found in database.";
            }

            // Delete location
            log.info("🗑️ Deleting location: '{}' with ID: {}", locationToRemove.getName(), locationId);
            locationService.delete(locationId);

            // Verify deletion
            if (locationService.exists(locationId)) {
                log.error("❌ Location ID '{}' still exists after deletion!", locationId);
                return "Error: Location was not removed successfully.";
            }

            // Notify WebSocket
            log.info("📡 Sending WebSocket notification to remove location: {}", locationId);
            webSocketService.sendLocationRemoved(sessionId, locationId);

            log.info("✅ Location '{}' removed successfully.", locationToRemove.getName());
            return String.format("✅ Successfully removed '%s' from the map.", locationToRemove.getName());

        } catch (Exception e) {
            log.error("❌ Error in RemoveLocationByNameFunction: {}", e.getMessage(), e);
            return "Error removing location: " + e.getMessage();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemoveLocationByNameRequest {
        @NotBlank(message = "Location name cannot be blank.")
        @JsonPropertyDescription("Required. The name of the location to remove (e.g., 'Eiffel Tower', 'Times Square'). Partial names are supported.")
        private String locationName;
    }
}
