package com.springboot.FlomadAIplanner.dto.google;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextSearchRequest {

    @NotBlank
    @JsonPropertyDescription("REQUIRED. The exact place name used to search for the more detailed information about the location of the required place. Example: 'Spicy Vegetarian Food in Sydney, Australia' or 'Time Square, New York'.")
    private String textQuery;

}
