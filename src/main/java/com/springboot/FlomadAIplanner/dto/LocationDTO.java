package com.springboot.FlomadAIplanner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDTO {
    @NotBlank
    private String sessionId; // Associates the location with a chat/session

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private String name; // Optional name or description of the location

    private String type; // E.g., "attraction", "cafe", "hotel"
}
