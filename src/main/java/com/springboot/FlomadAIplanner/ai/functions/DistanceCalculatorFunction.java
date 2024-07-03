package com.springboot.FlomadAIplanner.ai.functions;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Description("Calculates the distance between two locations.")
public class DistanceCalculatorFunction implements AIFunction<DistanceCalculatorFunction.DistanceCalculatorRequest, DistanceCalculatorFunction.DistanceCalculatorResponse> {

    @Override
    public DistanceCalculatorResponse apply(DistanceCalculatorRequest request) {
        log.info("Calculating distance between {} and {}", request.getFromLocation(), request.getToLocation());
        // Mock implementation
        return new DistanceCalculatorResponse("10 km");
    }

    @Data
    public static class DistanceCalculatorRequest {
        private String fromLocation;
        private String toLocation;
    }

    @Data
    public static class DistanceCalculatorResponse {
        private final String distance;
    }
}
