package com.springboot.FlomadAIplanner.ai.dto;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
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
            "TRAVEL ASSISTANT main response to the USER. This message will be displayed to the user." +
            "Must be a complete answer, while generating this value imagine that 'suggestions' don't exist at all.")
    private String text;

    @JsonPropertyDescription("Required." +
            "List of suggested next requests USER can logically send to TRAVEL ASSISTANT in the current chat context." +
            "While generating these suggestions, keep in mind that one of these suggestions could be a message from USER (client) to the TRAVEL ASSISTANT" +
            "Must be 4 items.")
    private List<String> suggestions;

}









