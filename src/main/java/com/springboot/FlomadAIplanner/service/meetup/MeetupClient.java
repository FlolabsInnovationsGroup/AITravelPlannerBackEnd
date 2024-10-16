package com.springboot.FlomadAIplanner.service.meetup;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.service.meetup.config.MeetupClientConfig;
import java.util.Map;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "meetupClient", url = "${meetup.api.baseurl}",
        configuration = MeetupClientConfig.class)
public interface MeetupClient {

    /**
     * Searches for events based on the provided request parameters.
     *
     * @param query A GraphQL query string.
     * @return A JSON response containing the search results.
     * @throws FeignException If an error occurs during the API call.
     */
    @PostMapping(value = "/graphql", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode searchEvents(@RequestBody Map<String, String> query);
}

