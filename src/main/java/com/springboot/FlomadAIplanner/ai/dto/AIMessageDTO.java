package com.springboot.FlomadAIplanner.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIMessageDTO {
    private String sessionId;
    private String message;
}
