package com.springboot.FlomadAIplanner.ai.dto;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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


    @JsonPropertyDescription("Required. " +
            "TRAVEL ASSISTANT response to the USER input. This message will be displayed to the user." +
            "Must be a complete friendly answer, based on the input. While generating this value imagine that 'suggestions' don't exist at all.")
    private String message;

    @JsonPropertyDescription("Required." +
            "List of suggested next requests USER can logically send to TRAVEL ASSISTANT in the current chat context, and You MUST also think about the future flow of the dialogue." +
            "Keep in mind that one of these suggestions will be a message from USER (client) to the TRAVEL ASSISTANT." +
            "Suggestions must be really useful." +
            "Must be 4 items.")
    private List<String> suggestions;

}









