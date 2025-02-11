package com.springboot.FlomadAIplanner.service.positionstack;

import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackResponse;
import com.springboot.FlomadAIplanner.service.positionstack.config.PositionStackClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "positionStackClient",
        url = "${positionstack.api.baseurl}",
        configuration = PositionStackClientConfig.class
)
public interface PositionStackClient {

    /**
     * Calls the /forward endpoint for a single address query.
     * Example: GET /forward?access_key=XYZ&query=Berlin&limit=1
     *
     * @param query   The textual address or place name.
     * @param limit   How many results to return (1..80).
     * @param country Optional: Filter by country code.
     * @param region  Optional: Filter by region, city, or state.
     * @param output  Optional: Desired output format (usually "json").
     * @return The geocoding results from PositionStack.
     */
    @GetMapping(value = "/forward", consumes = MediaType.APPLICATION_JSON_VALUE)
    PositionStackResponse forwardGeocode(
            @RequestParam("query") String query,
            @RequestParam("limit") Integer limit,
            @RequestParam("country") String country,
            @RequestParam("region") String region,
            @RequestParam("output") String output
    );
}
