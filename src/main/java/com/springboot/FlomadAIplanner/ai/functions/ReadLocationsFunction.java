package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.model.entities.Location;
import com.springboot.FlomadAIplanner.service.model.LocationService;
import com.springboot.FlomadAIplanner.util.RequestService;
import com.springboot.FlomadAIplanner.util.SessionContextHolder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
@Description("""
        Function to retrieve all locations associated with a specific session.
        
        Purpose:
        Use this function to retrieve all dots on the map for a given chat session.
        You MUST Use this Function BEFORE adding new locations to the map to avoid duplicates.
        You MUST Use this Function BEFORE removing locations from the map to receive
        the Ids of the existing locations to understand which one you need to remove.
        
        Output:
        Returns a list of locations.
        """)
public class ReadLocationsFunction implements Function<ReadLocationsFunction.ReadLocationsRequest, List<Location>> {
    private final LocationService locationService;
    private final RequestService requestService;

    public ReadLocationsFunction(LocationService locationService, RequestService requestService) {
        this.locationService = locationService;
        this.requestService = requestService;
    }

    @Override
    public List<Location> apply(ReadLocationsRequest request) {
        try {
            // Get sessionId from context
            String sessionId = SessionContextHolder.getSessionId();
            log.info("🔍 Extracted sessionId from context: {}", sessionId);

            if (sessionId == null || sessionId.isEmpty()) {
                log.error("❌ Failed to retrieve sessionId from context. ReadLocationsFunction cannot proceed.");
                return List.of();
            }

            List<Location> locations = locationService.findAllBySessionId(sessionId);
            log.info("📍 ReadLocationsFunction returning {} locations for session {}", locations.size(), sessionId);

            return locations;
        } catch (Exception e) {
            log.error("❌ Error in ReadLocationsFunction: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Data
    public static class ReadLocationsRequest {
        @JsonPropertyDescription("Just should be the value 'ALL'")
        private String command;
    }
}
