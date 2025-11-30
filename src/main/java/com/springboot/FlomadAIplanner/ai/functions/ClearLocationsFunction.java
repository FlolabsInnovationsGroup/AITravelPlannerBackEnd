package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.service.model.LocationService;
import com.springboot.FlomadAIplanner.service.socket.WebSocketService;
import com.springboot.FlomadAIplanner.util.SessionContextHolder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
@Description("""
        Function to CLEAR ALL locations.
        Purpose:
        This function MUST be used to remove ALL markers or points from the map
        whenever a user explicitly REQUESTS to clear the map OR when the CHAT CONTEXT REQUIRES resetting map locations.
        
        AI MUST call this function in scenarios SUCH AS:
        1. The user explicitly says, "clear the map," "reset locations,"
           or similar requests.
        2. The chat flow requires REPLACING existing map data with NEW DATA.
        3. ANY SITUATION where continuing with existing map markers would
           CONFLICT with the conversation's goal.
           
        IMPORTANT:
        YOU CANNOT RETURN A CONFIRMATION THAT THE MAP IS CLEARED IF YOU DID NOT RECEIVE A RESPONSE FROM THIS FUNCTION!
        
        Output:
        Returns a confirmation message after successfully clearing all locations.
        """)
public class ClearLocationsFunction implements Function<ClearLocationsFunction.ClearLocationsRequest, String> {
    private final LocationService locationService;
    private final WebSocketService webSocketService;

    public ClearLocationsFunction(LocationService locationService, WebSocketService webSocketService) {
        this.locationService = locationService;
        this.webSocketService = webSocketService;
    }

    @Override
    public String apply(ClearLocationsRequest request) {
        String sessionId;
        try {
            // Retrieve session ID from context
            log.info("🧹 Attempting to retrieve sessionId from context...");
            sessionId = SessionContextHolder.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                log.error("❌ Session ID is null or empty in context. Aborting clear operation.");
                return "Error: Session ID is missing or invalid.";
            }
            log.info("🚀 Starting ClearLocationsFunction for sessionId: {}", sessionId);

            // CRITICAL: Stop any pending WebSocket broadcasts to prevent race condition
            try {
                webSocketService.pauseBroadcasting(sessionId);
                log.info("🚫 Paused WebSocket broadcasting for sessionId: {}", sessionId);
            } catch (Exception e) {
                log.warn("⚠️ Could not pause broadcasting (method may not exist): {}", e.getMessage());
            }

            // Delete all locations associated with the session
            try {
                locationService.deleteAllBySessionId(sessionId);
                log.info("🗑️ All locations for sessionId {} cleared from the database.", sessionId);
            } catch (Exception e) {
                log.error("❌ Error deleting locations for sessionId {}: {}", sessionId, e.getMessage());
                return "Error: Unable to clear locations. Please try again.";
            }

            // Send WebSocket notification to clear the map
            try {
                webSocketService.sendMapCleared(sessionId);
                log.info("📡 Sent MAP_CLEARED WebSocket event for sessionId: {}", sessionId);
            } catch (Exception e) {
                log.error("❌ Error sending WebSocket clear message for sessionId {}: {}", sessionId, e.getMessage());
                return "Error: Unable to notify clients to clear the map.";
            }

            // Small delay to ensure clearing is processed before any new operations
            try {
                Thread.sleep(500); // 500ms delay to prevent immediate re-population
                log.info("⏰ Added 500ms delay to ensure clearing completes");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.warn("⚠️ Delay was interrupted");
            }

            // Resume broadcasting after clearing is complete
            try {
                webSocketService.resumeBroadcasting(sessionId);
                log.info("✅ Resumed WebSocket broadcasting for sessionId: {}", sessionId);
            } catch (Exception e) {
                log.warn("⚠️ Could not resume broadcasting (method may not exist): {}", e.getMessage());
            }

            return "✅ Your map has been successfully cleared! Ready to start fresh with your travel plans?";
        } catch (Exception e) {
            log.error("❌ Error in ClearLocationsFunction: {}", e.getMessage(), e);
            return "Error clearing locations: " + e.getMessage();
        }
    }


    @Data
    public static class ClearLocationsRequest {
        @JsonPropertyDescription("Just should be the value 'CLEAR'")
        private String command;
    }
}
