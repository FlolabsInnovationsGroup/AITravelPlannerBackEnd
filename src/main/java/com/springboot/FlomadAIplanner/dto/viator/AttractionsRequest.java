package com.springboot.FlomadAIplanner.dto.viator;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttractionsRequest {

    @NotNull(message = "destId is required")
    @Positive(message = "destId must be a positive integer")
    @JsonPropertyDescription("Required. Positive integer. Unique numeric identifier of the destination in which to search for attractions.")
    private int destId;

    @Pattern(regexp = "\\d+-\\d+", message = "topX must be in the format {start}-{end}")
    @JsonPropertyDescription("Optional. String in the format {start}-{end}. Specifies the start and end rows to return. Example: '1-10', '11-20'. The maximum number of rows per request is 100; if not specified, the default is '1-100'.")
    private String topX;

    @Pattern(regexp = "RECOMMENDED|SEO_ALPHABETICAL", message = "sortOrder must be one of 'RECOMMENDED' or 'SEO_ALPHABETICAL'")
    @JsonPropertyDescription("Optional. Specifies the sort order for the results. Allowed values: 'RECOMMENDED' – sorted in order of recommendation, 'SEO_ALPHABETICAL' – sorted alphabetically A->Z.")
    private String sortOrder;
}
