package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.model.entities.Location;
import com.springboot.FlomadAIplanner.service.model.LocationService;
import com.springboot.FlomadAIplanner.service.socket.WebSocketService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Description("""
        Function to add a new location to the map.

        Purpose:
        Use this function to add a new location (dot) to the map associated with a chat session.

        Output:
        Returns a confirmation message.
        """)
public class AddLocationFunction implements AIFunction<AddLocationFunction.AddLocationRequest, String> {
    private final LocationService locationService;
    private final WebSocketService webSocketService;

    public AddLocationFunction(LocationService locationService, WebSocketService webSocketService) {
        this.locationService = locationService;
        this.webSocketService = webSocketService;
    }

    @Override
    public String apply(AddLocationRequest request) {
        log.info("Starting AddLocationFunction with request: {}", request);
        try {
            Location location = Location.builder()
                    .sessionId(request.getSessionId())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .name(request.getName())
                    .type(request.getType())
                    .build();
            locationService.save(location);
            webSocketService.sendLocationUpdate(location);
            return "Location added successfully.";
        } catch (Exception e) {
            log.error("Error in AddLocationFunction: {}", e.getMessage(), e);
            return "Error adding location: " + e.getMessage();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddLocationRequest {
        @NotBlank(message = "Session ID cannot be blank.")
        @JsonPropertyDescription("Required. The ID of the chat session.")
        private String sessionId;

        @NotNull(message = "Latitude cannot be null.")
        @JsonPropertyDescription("Required. Latitude of the location.")
        private Double latitude;

        @NotNull(message = "Longitude cannot be null.")
        @JsonPropertyDescription("Required. Longitude of the location.")
        private Double longitude;

        @JsonPropertyDescription("Optional. Name or description of the location.")
        private String name;

        @JsonPropertyDescription("Optional. Type of the location (e.g., 'cafe', 'hotel').")
        private String type;
    }
}
