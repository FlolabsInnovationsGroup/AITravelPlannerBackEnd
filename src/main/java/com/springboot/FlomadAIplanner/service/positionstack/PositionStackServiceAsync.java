package com.springboot.FlomadAIplanner.service.positionstack;

import com.springboot.FlomadAIplanner.ai.functions.PositionStackGeocodingFunction;
import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackForwardGeocodingRequest;
import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackResponse;
import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackResponse.PositionStackData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Asynchronous version of PositionStack service.
 * Uses Spring @Async for non-blocking geocoding requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PositionStackServiceAsync {

    private final PositionStackClient positionStackClient;

    @Async
    public CompletableFuture<List<PositionStackData>> forwardGeocodeSingleQuery(PositionStackForwardGeocodingRequest req) {
        try {
            log.info("Async geocoding request: {}", req.getQuery());

            // Provide default values to prevent NullPointerException in Feign calls
            String query = req.getQuery();
            int limit = (req.getLimit() != null) ? req.getLimit() : 1;  // Default: 1 result
            String country = (req.getCountry() != null) ? req.getCountry() : "";
            String region = (req.getRegion() != null) ? req.getRegion() : "";
            String outputFormat = "json";  // Always return JSON

            // Call PositionStack API
            PositionStackResponse response = positionStackClient.forwardGeocode(query, limit, country, region, outputFormat);

            // Return results if valid, else empty list
            if (response != null && response.getData() != null) {
                log.info("Async geocoding response received for: {}", query);
                return CompletableFuture.completedFuture(response.getData());
            }
        } catch (Exception e) {
            log.error("Error during async geocoding call for: {}", req.getQuery(), e);
        }

        // Return empty list on failure
        return CompletableFuture.completedFuture(new ArrayList<>());
    }

    /**
     * Bulk geocoding asynchronously. Calls PositionStack for multiple addresses in parallel.
     */
    public CompletableFuture<List<PositionStackData>> bulkForwardGeocodeAsync(
            PositionStackGeocodingFunction.PositionStackForwardGeocodingRequestsWrapper wrapper) {
        log.info("Starting async bulk geocoding for {} queries", wrapper.getRequests().size());

        // Run all requests asynchronously
        List<CompletableFuture<List<PositionStackData>>> futureTasks = wrapper.getRequests().stream()
                .map(this::forwardGeocodeSingleQuery)
                .collect(Collectors.toList());

        // Combine all async results once all complete
        CompletableFuture<?>[] futures = futureTasks.toArray(new CompletableFuture<?>[0]);
        return CompletableFuture.allOf(futures)
                .thenApply(voidResult -> {
                    List<PositionStackData> combinedResults = new ArrayList<>();
                    futureTasks.forEach(task -> {
                        try {
                            combinedResults.addAll(task.get());  // Collect results from completed tasks
                        } catch (Exception e) {
                            log.error("Error retrieving async geocoding result", e);
                        }
                    });
                    log.info("Async bulk geocoding completed. Total results: {}", combinedResults.size());
                    return combinedResults;
                });
    }
}
