package com.springboot.FlomadAIplanner.ai.functions;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Description("Calculates the distance between lists of locations.")
public class DistanceCalculatorFunctionMock implements AIFunction<DistanceCalculatorFunctionMock.DistanceCalculatorRequest, DistanceCalculatorFunctionMock.DistanceCalculatorResponse> {

    @Override
    public DistanceCalculatorResponse apply(DistanceCalculatorRequest request) {
        log.info("Calculating distances between locations");
        // Mock implementation
        List<String> distances = new ArrayList<>();
        for (int i = 0; i < request.getFromLocations().size(); i++) {
            String fromLocation = request.getFromLocations().get(i);
            String toLocation = request.getToLocations().get(i);
            log.info("Calculating distance between {} and {}", fromLocation, toLocation);
            distances.add("10 km"); // Mock distance calculation
        }
        return new DistanceCalculatorResponse(distances);
    }

    @Data
    public static class DistanceCalculatorRequest {
        private List<String> fromLocations;
        private List<String> toLocations;
    }

    @Data
    public static class DistanceCalculatorResponse {
        private final List<String> distances;
    }
}
