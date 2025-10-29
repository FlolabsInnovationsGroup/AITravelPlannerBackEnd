package com.springboot.FlomadAIplanner.ai.dto;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**

 Represents the response from the AI, including the main message and suggested next inputs.
 The primary purpose of this response is to provide the user with actionable insights and
 contextually relevant suggestions for continuing their interaction with the system.
 <p>
 Suggestions should align with the user's current query and offer clear next steps or
 potential exploration paths. These suggestions are generated dynamically by the AI based
 on the user's input and are designed to enhance the conversational flow.*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonClassDescription("Response from the AI, including the main response message and suggested next inputs.")
public class AIMessageResponse {


    @NotNull(message = "AI response message cannot be null")
    @NotEmpty(message = "AI response message cannot be empty")
    @Size(min = 10, max = 5000, message = "AI response message must be between 10 and 5000 characters")
    @JsonPropertyDescription("Required. " +
            "TRAVEL ASSISTANT response to the USER input. This message will be displayed to the user." +
            "Must be a complete friendly answer, based on the input. While generating this value imagine that 'suggestions' don't exist at all.")
    private String message;

    @NotNull(message = "Suggestions list cannot be null")
    @Size(min = 3, max = 5, message = "Must provide between 3 and 5 suggestions")
    @JsonPropertyDescription("Required." +
            "List of suggested next requests USER can logically send to TRAVEL ASSISTANT in the current chat context, and You MUST also think about the future flow of the dialogue." +
            "Keep in mind that one of these suggestions will be a message from USER (client) to the TRAVEL ASSISTANT." +
            "Suggestions must be really useful." +
            "Must be 4 items.")
    private List<@NotEmpty(message = "Each suggestion cannot be empty") @Size(min = 5, max = 200, message = "Each suggestion must be between 5 and 200 characters") String> suggestions;

}









