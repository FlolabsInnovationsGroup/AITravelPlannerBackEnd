package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.meetup.SearchEventsRequest;
import com.springboot.FlomadAIplanner.exceptions.api.MeetupAPIException;
import com.springboot.FlomadAIplanner.service.meetup.MeetupService;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Description("""
        Use this function to search for events on Meetup based on various criteria such as location, date range, and keywords.
        This function accepts multiple search requests, allowing users to find events that match their interests and preferences.
        It is ideal for users who are looking for social activities and events in specific areas.
        Ensure to call this function after obtaining the user's location or preferences to provide the best suggestions.
        """)
public class EventSearchFunction implements AIFunction<EventSearchFunction.EventSearchRequestWrapper, List<JsonNode>> {

    private final MeetupService meetupService;

    @Autowired
    public EventSearchFunction(MeetupService meetupService) {
        this.meetupService = meetupService;
    }

    @Valid
    @Override
    public List<JsonNode> apply(EventSearchRequestWrapper requestWrapper) {
        List<JsonNode> responses = new ArrayList<>();

        for (SearchEventsRequest request : requestWrapper.getRequests()) {
            log.info("Searching for events in location: {} with date range: {} to {} and keywords: {}",
                    request.getLocation(),
                    request.getStartDate(),
                    request.getEndDate(),
                    String.join(", ", request.getKeywords()));
            try {
                JsonNode response = meetupService.searchEvents(request);
                log.info("Received event search response from Meetup. Response length: {}", response.toString().length());
                responses.add(response);
            } catch (MeetupAPIException e) {
                log.error("Error fetching events from Meetup", e);
                responses.add(JsonNodeFactory.instance.objectNode().put("error", e.getMessage()));
            }
        }

        return responses;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventSearchRequestWrapper {

        @Valid
        @JsonPropertyDescription("List of event search requests containing location, date range, and keywords.")
        private List<SearchEventsRequest> requests;
    }
}

