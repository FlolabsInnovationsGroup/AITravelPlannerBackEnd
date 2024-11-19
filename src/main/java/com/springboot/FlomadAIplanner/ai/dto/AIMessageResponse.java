package com.springboot.FlomadAIplanner.ai.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the response from the AI, including the main message and suggested next inputs.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIMessageResponse {

    @JsonPropertyDescription("The main message from the AI to the user.")
    private String text;

    @JsonPropertyDescription("Suggestions for the next user inputs. Up to 4.")
    private List<String> suggestions;
}
