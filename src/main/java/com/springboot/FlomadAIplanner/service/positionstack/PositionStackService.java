package com.springboot.FlomadAIplanner.service.positionstack;

import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackForwardGeocodingRequest;
import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackResponse;
import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackResponse.PositionStackData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for PositionStack API Geocoding.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PositionStackService {

    private final PositionStackClient positionStackClient;

    /**
     * Calls PositionStack's forward geocoding API for multiple addresses.
     */
    public List<PositionStackData> bulkForwardGeocode(List<PositionStackForwardGeocodingRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            log.warn("No requests provided for bulk geocoding.");
            return new ArrayList<>();
        }

        log.info("Performing bulk forward geocoding for {} requests.", requests.size());
        List<PositionStackData> allResults = new ArrayList<>();

        for (PositionStackForwardGeocodingRequest req : requests) {
            if (req == null || req.getQuery() == null || req.getQuery().isEmpty()) {
                log.warn("Skipping invalid geocoding request: {}", req);
                continue;
            }

            log.info("Forward geocoding address: {}", req.getQuery());

            try {
                PositionStackResponse response = positionStackClient.forwardGeocode(
                        req.getQuery(),
                        req.getLimit() != null ? req.getLimit() : 1,  // Default limit to 1
                        req.getCountry(),
                        req.getRegion(),
                        "json"
                );

                if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                    allResults.addAll(response.getData());
                } else {
                    log.warn("No geocoding data returned for query: {}", req.getQuery());
                }

            } catch (Exception e) {
                log.error("PositionStack API request failed for query: {}", req.getQuery(), e);
            }
        }

        log.info("Total combined geocoding results: {}", allResults.size());
        return allResults;
    }
}
