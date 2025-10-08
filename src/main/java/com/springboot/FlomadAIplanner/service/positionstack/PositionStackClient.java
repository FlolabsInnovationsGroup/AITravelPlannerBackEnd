package com.springboot.FlomadAIplanner.service.positionstack;

import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackResponse;
import com.springboot.FlomadAIplanner.service.positionstack.config.PositionStackClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "positionStackClient",
        url = "${position-stack.api.baseurl}",
        configuration = PositionStackClientConfig.class
)
public interface PositionStackClient {

    /**
     * Calls the /forward endpoint for a single address query.
     * Example: GET /forward?access_key=XYZ&query=Berlin&limit=1
     *
     * @param query   The textual address or place name.
     * @param limit   How many results to return (default 1).
     * @param country Optional: Filter by country code.
     * @param region  Optional: Filter by region, city, or state.
     * @param output  Optional: Desired output format (default "json").
     * @return The geocoding results from PositionStack.
     */
    @GetMapping(value = "/forward")
    PositionStackResponse forwardGeocode(
            @RequestParam("query") String query,
            @RequestParam(value = "limit", defaultValue = "1") Integer limit,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "output", defaultValue = "json") String output
    );
}
