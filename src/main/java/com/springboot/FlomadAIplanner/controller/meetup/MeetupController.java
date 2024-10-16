package com.springboot.FlomadAIplanner.controller.meetup;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.meetup.SearchEventsRequest;
import com.springboot.FlomadAIplanner.exceptions.api.MeetupAPIException;
import com.springboot.FlomadAIplanner.service.meetup.MeetupService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/meetup")
public class MeetupController {

    private final MeetupService meetupService;

    public MeetupController(MeetupService meetupService) {
        this.meetupService = meetupService;
    }

    /**
     * Endpoint to search for events.
     *
     * @param searchEventsRequest The request object containing search parameters.
     * @return JSON response with search results.
     * @throws MeetupAPIException If an error occurs while calling the Meetup API.
     */
    @Operation(summary = "Search for Meetup events", description = "Searches for events using the Meetup API")
    @PostMapping("/search-events")
    public JsonNode searchEvents(@RequestBody SearchEventsRequest searchEventsRequest) throws MeetupAPIException {
        log.info("Searching for Meetup events with request: {}", searchEventsRequest);
        return meetupService.searchEvents(searchEventsRequest);
    }
}


