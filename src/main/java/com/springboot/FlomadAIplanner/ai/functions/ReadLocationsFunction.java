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
        Retrieves all locations currently displayed on the user's map for the active session. Essential 
        for understanding current itinerary state and preventing duplicate additions.
        
        CORE PURPOSE: Map state awareness and duplicate prevention
        
        CRITICAL WORKFLOW: ALWAYS call BEFORE adding new locations to avoid duplicates and understand current map state
        
        KEY FEATURES:
        - Complete session location inventory
        - Location details (IDs, names, coordinates)
        - Duplicate prevention support
        - Current map state analysis
        
        PERFECT FOR:
        - "What's currently on my map?"
        - Pre-addition duplicate checking
        - Itinerary state verification
        - Session location management
        
        OPTIMIZATION NOTE: For location removal by name, use RemoveLocationByNameFunction directly instead of calling this + RemoveLocationFunction
        
        SMART USAGE:
        - Check current map state before modifications
        - Verify location existence before operations
        - Maintain session location awareness
        
        Returns: Complete list of locations with IDs, names, coordinates, and session details
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
