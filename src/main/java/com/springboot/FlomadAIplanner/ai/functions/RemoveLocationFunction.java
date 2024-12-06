package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.ai.dto.AIMessageDTO;
import com.springboot.FlomadAIplanner.service.model.LocationService;
import com.springboot.FlomadAIplanner.service.socket.WebSocketService;
import com.springboot.FlomadAIplanner.util.RequestService;
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
        Function to remove a specific location by ID.
        
        Purpose:
        Use this function to remove a dot from the map.
        Use this function If this object is no longer discussed in the context of the dialogue and its display on the map will only interfere and confuse the user. Only relevant objects that are discussed should be displayed on the map.
        
        Notes:
        You MUST Use this Function AFTER reading all locations to understand which one you need to remove.
        
        Output:
        Returns a confirmation message.
        """)
public class RemoveLocationFunction implements Function<RemoveLocationFunction.RemoveLocationRequest, String> {
    private final LocationService locationService;
    private final WebSocketService webSocketService;
    private final RequestService requestService;

    public RemoveLocationFunction(LocationService locationService, WebSocketService webSocketService, RequestService requestService) {
        this.locationService = locationService;
        this.webSocketService = webSocketService;
        this.requestService = requestService;
    }

    @Override
    public String apply(RemoveLocationRequest request) {
        try {
            String sessionId = requestService.getParameterFromRequest(AIMessageDTO.class.getDeclaredField("sessionId").getName());
            log.info("Starting RemoveLocationFunction for locationId: {}; in session: {}", request.getLocationId(), sessionId);

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
