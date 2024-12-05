package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.service.model.LocationService;
import com.springboot.FlomadAIplanner.service.socket.WebSocketService;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Description("""
        Function to remove a specific location by ID.
        
        Purpose:
        Use this function to remove a dot from the map.
        
        Output:
        Returns a confirmation message.
        """)
public class RemoveLocationFunction extends AbstractLocationFunction implements AIFunction<RemoveLocationFunction.RemoveLocationRequest, String> {
    private final LocationService locationService;
    private final WebSocketService webSocketService;

    public RemoveLocationFunction(LocationService locationService, WebSocketService webSocketService) {
        this.locationService = locationService;
        this.webSocketService = webSocketService;
    }

    @Override
    public String apply(RemoveLocationRequest request) {
        String sessionId = getSessionIdFromHttpRequest();
        log.info("Starting RemoveLocationFunction for locationId: {}; in session: {}", request.getLocationId(), sessionId);
        try {
            locationService.delete(request.getLocationId());
            webSocketService.sendLocationRemoved(sessionId, request.getLocationId());
            return "Location removed successfully.";
        } catch (Exception e) {
            log.error("Error in RemoveLocationFunction: {}", e.getMessage(), e);
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
