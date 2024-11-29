package com.springboot.FlomadAIplanner.ai.functions;

import com.springboot.FlomadAIplanner.service.model.LocationService;
import com.springboot.FlomadAIplanner.service.socket.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Description("""
        Function to clear all locations associated with a specific session.

        Purpose:
        Use this function to remove all dots from the map for a given chat session.

        Output:
        Returns a confirmation message.
        """)
public class ClearLocationsFunction implements AIFunction<String, String> {
    private final LocationService locationService;
    private final WebSocketService webSocketService;

    public ClearLocationsFunction(LocationService locationService, WebSocketService webSocketService) {
        this.locationService = locationService;
        this.webSocketService = webSocketService;
    }

    @Override
    public String apply(String sessionId) {
        log.info("Starting ClearLocationsFunction for sessionId: {}", sessionId);
        try {
            locationService.deleteAllBySessionId(sessionId);
            webSocketService.sendMapCleared(sessionId);
            return "All locations cleared successfully.";
        } catch (Exception e) {
            log.error("Error in ClearLocationsFunction: {}", e.getMessage(), e);
            return "Error clearing locations: " + e.getMessage();
        }
    }
}
