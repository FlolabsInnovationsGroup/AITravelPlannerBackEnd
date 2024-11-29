package com.springboot.FlomadAIplanner.ai.functions;

import com.springboot.FlomadAIplanner.model.entities.Location;
import com.springboot.FlomadAIplanner.service.model.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Description("""
        Function to retrieve all locations associated with a specific session.

        Purpose:
        Use this function to retrieve all dots on the map for a given chat session.

        Output:
        Returns a list of locations.
        """)
public class ReadLocationsFunction implements AIFunction<String, List<Location>> {
    private final LocationService locationService;

    public ReadLocationsFunction(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public List<Location> apply(String sessionId) {
        log.info("Starting ReadLocationsFunction for sessionId: {}", sessionId);
        return locationService.findAllBySessionId(sessionId);
    }
}
