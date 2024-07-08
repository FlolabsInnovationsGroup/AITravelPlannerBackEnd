package com.springboot.FlomadAIplanner.dto;
import java.util.List;

import com.springboot.FlomadAIplanner.ai.service.FunctionNameSuggestion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for suggested properties from the chat client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatClientSuggestedProperties {
    private String systemPrompt;
    private List<FunctionNameSuggestion> requiredFunctions;
}