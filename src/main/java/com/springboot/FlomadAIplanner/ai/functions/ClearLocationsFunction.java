package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.service.model.LocationService;
import com.springboot.FlomadAIplanner.service.socket.WebSocketService;
import com.springboot.FlomadAIplanner.util.RequestService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
@Description("""
        Function to clear all locations associated with a specific session.
        
        Purpose:
        Use this function to remove all dots from the map for a given chat session.
        
        Output:
        Returns a confirmation message.
        """)
public class ClearLocationsFunction implements Function<ClearLocationsFunction.ClearLocationsRequest, String> {
    private final LocationService locationService;
    private final WebSocketService webSocketService;
    private final RequestService requestService;

    public ClearLocationsFunction(LocationService locationService, WebSocketService webSocketService, RequestService requestService) {
        this.locationService = locationService;
        this.webSocketService = webSocketService;
        this.requestService = requestService;
    }

    @Override
    public String apply(ClearLocationsRequest request) {
        try {
            String sessionId = requestService.getParameterFromRequest(AIMessageDTO.class.getDeclaredField("sessionId").getName());
            log.info("Starting ClearLocationsFunction for sessionId: {}", sessionId);

            locationService.deleteAllBySessionId(sessionId);
            webSocketService.sendMapCleared(sessionId);
            return "All locations cleared successfully.";
        } catch (Exception e) {
            log.error("Error in ClearLocationsFunction: {}", e.getMessage(), e);
            return "Error clearing locations: " + e.getMessage();
        }
    }

    @Data
    public static class ClearLocationsRequest {
        @JsonPropertyDescription("Just should be the value 'CLEAR'")
        private String command;
    }
}
