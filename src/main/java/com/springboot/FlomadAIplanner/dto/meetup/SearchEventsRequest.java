package com.springboot.FlomadAIplanner.dto.meetup;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchEventsRequest {

    @NotBlank
    @JsonPropertyDescription("REQUIRED. The location to search for events.")
    private String location;

    @JsonPropertyDescription("The start date for the event search.")
    private String startDate;

    @JsonPropertyDescription("The end date for the event search.")
    private String endDate;

    @Positive
    @JsonPropertyDescription("REQUIRED. Specifies the maximum number of results to return.")
    private int maxResults;

    @JsonPropertyDescription("OPTIONAL. A keyword to filter events.")
    private List<String> keywords;
}

