package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
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

import java.util.function.Function;

@Slf4j
@Component
@Description("""
        Function to remove a specific location by its exact ID.
        
        Purpose:
        Use this function ONLY when you have the exact location ID to remove.
        This is a low-level function typically used by other functions or when you already know the specific ID.
        
        IMPORTANT: If a user asks to remove a location by name (like "Eiffel Tower"), 
        use RemoveLocationByNameFunction instead, which handles the name-to-ID lookup automatically.
        
        Notes:
        You MUST Use this Function AFTER reading all locations to understand which one you need to remove.
        For user requests like "remove the Eiffel Tower", use RemoveLocationByNameFunction instead.
        
        Output:
        Returns a confirmation message.
        """)
public class RemoveLocationFunction implements Function<RemoveLocationFunction.RemoveLocationRequest, String> {

    private final LocationService locationService;
    private final WebSocketService webSocketService;

    public RemoveLocationFunction(LocationService locationService, WebSocketService webSocketService) {
        this.locationService = locationService;
        this.webSocketService = webSocketService;
    }

    @Override
    public String apply(RemoveLocationRequest request) {
        log.info("🔍 Attempting to remove location with ID: {}", request.getLocationId());

        try {
            // Ensure location exists before deleting
            if (!locationService.exists(request.getLocationId())) {
                log.warn("⚠️ Location ID '{}' not found in database!", request.getLocationId());
                return "Location not found.";
            }

            // Get session ID from context
            String sessionId = SessionContextHolder.getSessionId();
            log.info("📌 Session ID retrieved from context: {}", sessionId);

            // Delete location
            log.info("🗑 Deleting location ID: {}", request.getLocationId());
            locationService.delete(request.getLocationId());

            // Verify deletion
            if (locationService.exists(request.getLocationId())) {
                log.error("❌ Location ID '{}' still exists after deletion!", request.getLocationId());
                return "Error: Location was not removed.";
            }

            // Notify WebSocket
            log.info("📡 Sending WebSocket notification to remove location: {}", request.getLocationId());
            webSocketService.sendLocationRemoved(sessionId, request.getLocationId());

            log.info("✅ Location ID '{}' removed successfully.", request.getLocationId());
            return "Location removed successfully.";
        } catch (Exception e) {
            log.error("❌ Error in RemoveLocationFunction: {}", e.getMessage(), e);
            return "Error removing location: " + e.getMessage();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemoveLocationRequest {
        @NotBlank(message = "Location ID cannot be blank.")
        @JsonPropertyDescription("Required. The ID of the location to remove.")
        private String locationId;
    }
}
