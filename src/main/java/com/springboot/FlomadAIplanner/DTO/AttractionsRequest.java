package com.springboot.FlomadAIplanner.DTO;

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
    private int destId;

    @Pattern(regexp = "\\d+-\\d+", message = "topX must be in the format {start}-{end}")
    private String topX;

    @Pattern(regexp = "RECOMMENDED|SEO_ALPHABETICAL", message = "sortOrder must be one of 'RECOMMENDED' or 'SEO_ALPHABETICAL'")
    private String sortOrder;
}
