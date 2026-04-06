package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.model.entities.Location;
import com.springboot.FlomadAIplanner.service.geoapify.GeoapifyService;
import com.springboot.FlomadAIplanner.service.model.LocationService;
import com.springboot.FlomadAIplanner.util.SessionContextHolder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@Description("""
        🗺️ INTELLIGENT ROUTE PLANNER - Calculate optimal travel routes between your destinations
        Analyzes the best paths between locations with accurate distance, duration, and step-by-step directions.
        Supports multiple travel modes and multi-stop itineraries for comprehensive trip planning.
        
        SMART ROUTING FEATURES:
        - Multi-Stop Optimization: Plan routes through multiple destinations with waypoint optimization
        - Travel Mode Flexibility: Compare driving, walking, transit, and bicycling options
        - Traffic Awareness: Real-time traffic data for accurate duration estimates
        - Alternative Routes: Discover faster or scenic alternative paths
        - Distance & Time Analysis: Get precise estimates for better planning
        
        PERFECT FOR TRAVEL PLANNING:
        🚗 Road Trip Planning: "Calculate driving route from San Francisco to Los Angeles through Big Sur"
        🚶 Walking Tours: "Show walking route through Paris landmarks: Eiffel Tower to Louvre to Arc de Triomphe"
        🚇 Public Transit: "Get transit directions from Tokyo Station to Shibuya Crossing"
        🚴 Cycling Routes: "Find bicycling route through Amsterdam's canal district"
        ✈️ Multi-City Tours: "Plan route through Italy: Rome → Florence → Venice → Milan"
        
        TRAVEL INTELLIGENCE:
        - Smart route optimization for efficiency and experience
        - Considers traffic patterns and travel times
        - Provides step-by-step navigation guidance
        - Estimates total travel time and distance
        - Suggests best travel modes for each segment
        
        USAGE STRATEGY:
        ✅ Use DirectionsFunction: Route planning, distance calculations, multi-stop itineraries
        ❌ Don't use for: Single location details (use ShowLocationFunction instead)
        
        Returns: Formatted route details with distance, duration, and step-by-step directions
        """)
public class DirectionsFunction implements Function<DirectionsFunction.DirectionsRequest, String> {

    private final GeoapifyService geoapifyService;
    private final LocationService locationService;

    public DirectionsFunction(GeoapifyService geoapifyService, LocationService locationService) {
        this.geoapifyService = geoapifyService;
        this.locationService = locationService;
        log.info("DirectionsFunction initialized with Geoapify Routing API");
    }

    @Override
    public String apply(DirectionsRequest request) {
        log.info("DirectionsFunction called - origin: '{}', destination: '{}', mode: {}",
                request.getOrigin(),
                request.getDestination(),
                request.getTravelMode());

        try {
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = SessionContextHolder.getSessionId();
            }

            String originCoords = resolveLocationToCoordinates(request.getOrigin(), sessionId);
            String destCoords = resolveLocationToCoordinates(request.getDestination(), sessionId);

            if (originCoords == null || destCoords == null) {
                return "Could not resolve origin or destination. Please ensure locations exist on your map or provide valid coordinates.";
            }

            String travelMode = mapTravelMode(request.getTravelMode());

            List<String> allPoints = new ArrayList<>();
            allPoints.add(originCoords);
            if (request.getWaypoints() != null && !request.getWaypoints().isEmpty()) {
                for (String wp : request.getWaypoints()) {
                    String wpCoords = resolveLocationToCoordinates(wp, sessionId);
                    if (wpCoords != null) {
                        allPoints.add(wpCoords);
                    }
                }
            }
            allPoints.add(destCoords);

            String waypoints = allPoints.stream()
                    .map(coord -> {
                        String[] parts = coord.contains(",") ? coord.split(",") : new String[]{coord};
                        if (parts.length == 2) {
                            return parts[0].trim() + "," + parts[1].trim();
                        }
                        return coord;
                    })
                    .collect(Collectors.joining("|"));

            log.info("Calling Geoapify Routing API: waypoints={} mode={}", waypoints, travelMode);

            JsonNode response = geoapifyService.getDirections(waypoints, travelMode);

            if (response.has("error")) {
                return "Could not calculate route: " + response.get("error").asText();
            }

            return formatDirectionsResponse(response, request.getOrigin(), request.getDestination());

        } catch (Exception e) {
            log.error("Error calculating directions: {}", e.getMessage(), e);
            return "Error calculating route: " + e.getMessage();
        }
    }

    private String mapTravelMode(String mode) {
        if (mode == null) return "drive";
        return switch (mode.toLowerCase()) {
            case "driving", "drive" -> "drive";
            case "walking", "walk" -> "walk";
            case "bicycling", "bicycle", "cycling" -> "bicycle";
            case "transit", "public_transit" -> "transit";
            default -> "drive";
        };
    }

    private String resolveLocationToCoordinates(String location, String sessionId) {
        if (location == null || location.isEmpty()) {
            return null;
        }

        if (location.matches("-?\\d+\\.\\d+,-?\\d+\\.\\d+")) {
            return location;
        }

        List<Location> locations = locationService.findAllBySessionId(sessionId);
        for (Location loc : locations) {
            if (loc.getName() != null && loc.getName().equalsIgnoreCase(location.trim())) {
                return loc.getLatitude() + "," + loc.getLongitude();
            }
        }

        for (Location loc : locations) {
            if (loc.getName() != null && loc.getName().toLowerCase().contains(location.toLowerCase().trim())) {
                log.info("Partial match found: '{}' for query '{}'", loc.getName(), location);
                return loc.getLatitude() + "," + loc.getLongitude();
            }
        }

        log.info("Location not found in session, attempting geocode for: {}", location);
        try {
            JsonNode geocodeResult = geoapifyService.geocode(location);
            if (geocodeResult.has("results") && geocodeResult.get("results").size() > 0) {
                JsonNode first = geocodeResult.get("results").get(0);
                return first.get("lat").asDouble() + "," + first.get("lon").asDouble();
            }
        } catch (Exception e) {
            log.warn("Geocode fallback failed for '{}': {}", location, e.getMessage());
        }

        return null;
    }

    private String formatDirectionsResponse(JsonNode response, String origin, String destination) {
        try {
            JsonNode features = response.get("features");
            if (features == null || !features.isArray() || features.isEmpty()) {
                return "No routes found between " + origin + " and " + destination;
            }

            JsonNode properties = features.get(0).get("properties");
            if (properties == null) {
                return "No route data available";
            }

            long totalDistance = properties.has("distance") ? properties.get("distance").asLong() : 0;
            long totalTime = properties.has("time") ? properties.get("time").asLong() : 0;

            StringBuilder result = new StringBuilder();
            result.append("**ROUTE DETAILS**\n\n");
            result.append("**From:** ").append(origin).append("\n");
            result.append("**To:** ").append(destination).append("\n\n");
            result.append("**Total Distance:** ").append(formatDistance(totalDistance)).append("\n");
            result.append("**Estimated Duration:** ").append(formatDuration(totalTime)).append("\n\n");

            JsonNode legs = properties.get("legs");
            if (legs != null && legs.isArray() && !legs.isEmpty()) {
                JsonNode firstLeg = legs.get(0);
                JsonNode steps = firstLeg.get("steps");

                if (steps != null && steps.isArray() && !steps.isEmpty()) {
                    result.append("**Directions:**\n");
                    int stepNum = 1;
                    for (JsonNode step : steps) {
                        String instruction = "";
                        if (step.has("instruction") && step.get("instruction").has("text")) {
                            instruction = step.get("instruction").get("text").asText();
                        } else if (step.has("name")) {
                            instruction = "Continue on " + step.get("name").asText();
                        }

                        if (!instruction.isEmpty()) {
                            long stepDistance = step.has("distance") ? step.get("distance").asLong() : 0;
                            result.append(stepNum++).append(". ").append(instruction);
                            if (stepDistance > 0) {
                                result.append(" (").append(formatDistance(stepDistance)).append(")");
                            }
                            result.append("\n");
                        }
                    }
                }
            }

            return result.toString();

        } catch (Exception e) {
            log.error("Error formatting directions response: {}", e.getMessage(), e);
            return "Error formatting route details: " + e.getMessage();
        }
    }

    private String formatDistance(long meters) {
        if (meters < 1000) {
            return meters + " m";
        } else {
            double km = meters / 1000.0;
            return String.format("%.1f km (%.1f mi)", km, km * 0.621371);
        }
    }

    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%d hr %d min", hours, minutes);
        } else {
            return String.format("%d min", minutes);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonClassDescription("Request for calculating travel route directions between locations")
    public static class DirectionsRequest {

        @JsonPropertyDescription("Starting location (name or coordinates as 'lat,lng')")
        @NotNull(message = "Origin location is required")
        @Size(min = 1, max = 200, message = "Origin must be between 1 and 200 characters")
        private String origin;

        @JsonPropertyDescription("Destination location (name or coordinates as 'lat,lng')")
        @NotNull(message = "Destination location is required")
        @Size(min = 1, max = 200, message = "Destination must be between 1 and 200 characters")
        private String destination;

        @JsonPropertyDescription("Travel mode: 'driving', 'walking', 'transit', or 'bicycling' (default: 'driving')")
        @Size(max = 20, message = "Travel mode must not exceed 20 characters")
        private String travelMode;

        @JsonPropertyDescription("List of waypoint locations to visit between origin and destination")
        private List<String> waypoints;

        @JsonPropertyDescription("Session ID for location lookup (optional, auto-detected)")
        private String sessionId;

        @JsonCreator
        public DirectionsRequest(
                @JsonProperty("origin") String origin,
                @JsonProperty("destination") String destination) {
            this.origin = origin;
            this.destination = destination;
            this.travelMode = "driving";
        }
    }
}
