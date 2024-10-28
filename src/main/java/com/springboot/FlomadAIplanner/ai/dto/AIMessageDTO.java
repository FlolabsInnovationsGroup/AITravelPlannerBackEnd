package com.springboot.FlomadAIplanner.ai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIMessageDTO {
    @NotNull(message = "Session ID cannot be null")
    private String sessionId;
    @NotNull(message = "Message cannot be null")
    private String message;
}
