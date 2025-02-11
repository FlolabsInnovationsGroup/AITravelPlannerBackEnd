package com.springboot.FlomadAIplanner.service.positionstack;

import com.springboot.FlomadAIplanner.ai.functions.PositionStackGeocodingFunction;
import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackForwardGeocodingRequest;
import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackResponse;
import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackResponse.PositionStackData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Synchronous service for calling PositionStack forward geocoding endpoint.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PositionStackService {

    private final PositionStackClient positionStackClient;

    /**
     * Performs a batch forward geocoding: calls the PositionStack API for each request
     * and returns a single list containing all found geocoding results.
     *
     * @param wrapper The wrapper containing multiple textual addresses/queries.
     * @return A merged list of PositionStackData items across all queries.
     */
    public List<PositionStackData> bulkForwardGeocode(PositionStackGeocodingFunction.PositionStackForwardGeocodingRequestsWrapper wrapper) {
        log.info("Performing bulk forward geocoding for {} requests.", wrapper.getRequests().size());

        List<PositionStackData> allResults = new ArrayList<>();

        for (PositionStackForwardGeocodingRequest req : wrapper.getRequests()) {
            log.info("Forward geocoding address: {}", req.getQuery());

            // If 'output' is null or empty, we can pass "json" or simply let the Feign client default
            String outputFormat = (req.getOutput() == null || req.getOutput().isEmpty())
                    ? "json"
                    : req.getOutput();

            // Call the Feign client
            PositionStackResponse response = positionStackClient.forwardGeocode(
                    req.getQuery(),
                    req.getLimit(),
                    req.getCountry(),
                    req.getRegion(),
                    outputFormat
            );

            if (response != null && response.getData() != null) {
                allResults.addAll(response.getData());
            } else {
                log.warn("No data returned for query: {}", req.getQuery());
            }
        }

        log.info("Total combined geocoding results: {}", allResults.size());
        return allResults;
    }
}
