package com.springboot.FlomadAIplanner.service.meetup;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.exceptions.api.MeetupAPIException;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import com.springboot.FlomadAIplanner.dto.meetup.SearchEventsRequest;

@Slf4j
@Service
public class MeetupService {

    private final MeetupClient meetupClient;

    public MeetupService(MeetupClient meetupClient) {
        this.meetupClient = meetupClient;
    }

    /**
     * Searches for events using the Meetup API.
     *
     * @param searchEventsRequest The request object containing search parameters.
     * @return JSON response with search results.
     * @throws MeetupAPIException If an error occurs while calling the Meetup API.
     */
    public JsonNode searchEvents(SearchEventsRequest searchEventsRequest) throws MeetupAPIException {
        String query = buildGraphQLQuery(searchEventsRequest);
        try {
            Map<String, String> request = new HashMap<>();
            request.put("query", query);
            return meetupClient.searchEvents(request);
        } catch (FeignException e) {
            log.error("Error occurred while calling Meetup API: {}", e.getMessage());
            throw new MeetupAPIException("Error fetching events from Meetup", e);
        }
    }

    private String buildGraphQLQuery(SearchEventsRequest request) {
        // Construct the GraphQL query string.
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("{ findEvents(location: \"")
                    .append(request.getLocation())
                    .append("\", maxResults: ")
                    .append(request.getMaxResults());
        
        if (request.getKeywords() != null && !request.getKeywords().isEmpty()) {
            queryBuilder.append(", keyword: \"").append(request.getKeywords()).append("\"");
        }
        
        queryBuilder.append(") { id name description } }");
        return queryBuilder.toString();
    }
}
