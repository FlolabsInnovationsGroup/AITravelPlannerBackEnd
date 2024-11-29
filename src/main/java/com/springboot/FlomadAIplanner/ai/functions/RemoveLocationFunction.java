package com.springboot.FlomadAIplanner.ai.functions;

import com.springboot.FlomadAIplanner.service.model.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Description("""
        Function to remove a specific location by ID.

        Purpose:
        Use this function to remove a dot from the map.

        Output:
        Returns a confirmation message.
        """)
public class RemoveLocationFunction implements AIFunction<String, String> {
    private final LocationService locationService;

    public RemoveLocationFunction(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public String apply(String locationId) {
        log.info("Starting RemoveLocationFunction for locationId: {}", locationId);
        try {
            locationService.delete(locationId);
            return "Location removed successfully.";
        } catch (Exception e) {
            log.error("Error in RemoveLocationFunction: {}", e.getMessage(), e);
            return "Error removing location: " + e.getMessage();
        }
    }
}
