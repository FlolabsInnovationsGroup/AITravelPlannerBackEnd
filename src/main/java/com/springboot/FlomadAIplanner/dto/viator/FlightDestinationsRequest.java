package com.springboot.FlomadAIplanner.dto.viator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightDestinationsRequest {
    private String origin;
    private String maxPrice;
}
