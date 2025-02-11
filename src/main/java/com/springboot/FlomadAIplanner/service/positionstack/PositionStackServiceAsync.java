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

/**
 * Asynchronous version of PositionStack service.
 * Useful if you want to parallelize calls or not block the main thread.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PositionStackServiceAsync {

    private final PositionStackClient positionStackClient;

    @Async
    public CompletableFuture<List<PositionStackData>> forwardGeocodeSingleQuery(PositionStackForwardGeocodingRequest req) {
        try {
            String outputFormat = (req.getOutput() == null || req.getOutput().isEmpty()) ? "json" : req.getOutput();
            PositionStackResponse response = positionStackClient.forwardGeocode(
                    req.getQuery(),
                    req.getLimit(),
                    req.getCountry(),
                    req.getRegion(),
                    outputFormat
            );
            if (response != null && response.getData() != null) {
                return CompletableFuture.completedFuture(response.getData());
            }
        } catch (Exception e) {
            log.error("Error during async geocoding call for query: {}", req.getQuery(), e);
        }
        // Return empty list in case of failure
        return CompletableFuture.completedFuture(new ArrayList<>());
    }

    /**
     * Bulk geocoding in parallel. Each query is called asynchronously, results combined after all complete.
     */
    public List<PositionStackData> bulkForwardGeocodeAsync(PositionStackGeocodingFunction.PositionStackForwardGeocodingRequestsWrapper wrapper) {
        // Start tasks for each request
        List<CompletableFuture<List<PositionStackData>>> tasks = wrapper.getRequests().stream()
                .map(this::forwardGeocodeSingleQuery)
                .toList();

        // Wait until all tasks finish
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();

        // Combine results
        List<PositionStackData> combined = new ArrayList<>();
        tasks.forEach(task -> {
            try {
                combined.addAll(task.get());
            } catch (Exception e) {
                log.error("Exception in async result retrieval", e);
            }
        });

        log.info("Async forward geocoding completed. Total results: {}", combined.size());
        return combined;
    }
}
