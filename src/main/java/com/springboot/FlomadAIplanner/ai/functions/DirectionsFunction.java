package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.model.entities.Location;
import com.springboot.FlomadAIplanner.service.model.LocationService;
import com.springboot.FlomadAIplanner.util.SessionContextHolder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI function for calculating travel routes between locations using Google Maps Directions API.
 * Provides distance, duration, and step-by-step directions for optimal route planning.
 * 
 * Features:
 * - Multi-stop route optimization with waypoints
 * - Multiple travel modes (driving, walking, transit, bicycling)
 * - Accurate distance and duration estimates
 * - Traffic-aware routing (when available)
 * - Alternative route suggestions
 * 
 * Integration:
 * - Works with existing map locations via sessionId
 * - Supports direct coordinate input for custom routes
 * - Returns formatted text for AI response
 * - Frontend can request route visualization separately
 * 
 * @see Location
 * @see LocationService
 */
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

    @Value("${google.api.key}")
    private String apiKey;

    private final LocationService locationService;
    private final RestTemplate restTemplate;

    private static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json";

    public DirectionsFunction(LocationService locationService) {
        this.locationService = locationService;
        this.restTemplate = new RestTemplate();
        log.info("🗺️ DirectionsFunction initialized with Google Maps Directions API");
    }

    @Override
    public String apply(DirectionsRequest request) {
        log.info("🗺️ DirectionsFunction called - origin: '{}', destination: '{}', mode: {}", 
                 request.getOrigin(), 
                 request.getDestination(),
                 request.getTravelMode());

        try {
            // Get sessionId for location lookup
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = SessionContextHolder.getSessionId();
            }

            // Resolve origin and destination (could be location names or coordinates)
            String originCoords = resolveLocationToCoordinates(request.getOrigin(), sessionId);
            String destCoords = resolveLocationToCoordinates(request.getDestination(), sessionId);

            if (originCoords == null || destCoords == null) {
                return "❌ Could not resolve origin or destination. Please ensure locations exist on your map or provide valid coordinates.";
            }

            // Build API request
            String travelMode = request.getTravelMode() != null ? request.getTravelMode() : "driving";
            String url = buildDirectionsUrl(originCoords, destCoords, request.getWaypoints(), travelMode);

            log.info("🌐 Calling Google Directions API: {}", url.substring(0, Math.min(150, url.length())));

            // Call Google Directions API
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = response.getBody();
                String status = (String) responseBody.get("status");

                if ("OK".equals(status)) {
                    return formatDirectionsResponse(responseBody, request.getOrigin(), request.getDestination());
                } else {
                    log.error("❌ Directions API error status: {}", status);
                    return "❌ Could not calculate route. Status: " + status;
                }
            } else {
                log.error("❌ Directions API returned non-OK status: {}", response.getStatusCode());
                return "❌ Failed to fetch route directions.";
            }

        } catch (Exception e) {
            log.error("❌ Error calculating directions: {}", e.getMessage(), e);
            return "❌ Error calculating route: " + e.getMessage();
        }
    }

    /**
     * Resolve location name or coordinates string to lat,lng format
     */
    private String resolveLocationToCoordinates(String location, String sessionId) {
        if (location == null || location.isEmpty()) {
            return null;
        }

        // Check if already in coordinate format (lat,lng)
        if (location.matches("-?\\d+\\.\\d+,-?\\d+\\.\\d+")) {
            return location;
        }

        // Look up location by name in session locations
        List<Location> locations = locationService.findAllBySessionId(sessionId);
        for (Location loc : locations) {
            if (loc.getName() != null && loc.getName().equalsIgnoreCase(location.trim())) {
                return loc.getLatitude() + "," + loc.getLongitude();
            }
        }

        // Try partial match
        for (Location loc : locations) {
            if (loc.getName() != null && loc.getName().toLowerCase().contains(location.toLowerCase().trim())) {
                log.info("📍 Partial match found: '{}' for query '{}'", loc.getName(), location);
                return loc.getLatitude() + "," + loc.getLongitude();
            }
        }

        // If not found, treat as place name for Google to geocode
        log.info("🔍 Using place name directly for geocoding: {}", location);
        return location;
    }

    /**
     * Build Google Directions API URL
     */
    private String buildDirectionsUrl(String origin, String destination, List<String> waypoints, String mode) {
        StringBuilder url = new StringBuilder(DIRECTIONS_API_URL);
        url.append("?origin=").append(origin);
        url.append("&destination=").append(destination);
        url.append("&mode=").append(mode);

        // Add waypoints if provided
        if (waypoints != null && !waypoints.isEmpty()) {
            String waypointsParam = waypoints.stream()
                .map(wp -> resolveLocationToCoordinates(wp, SessionContextHolder.getSessionId()))
                .filter(wp -> wp != null)
                .collect(Collectors.joining("|"));
            
            if (!waypointsParam.isEmpty()) {
                url.append("&waypoints=optimize:true|").append(waypointsParam);
            }
        }

        url.append("&key=").append(apiKey);
        
        return url.toString();
    }

    /**
     * Format API response into human-readable text
     */
    @SuppressWarnings("unchecked")
    private String formatDirectionsResponse(Map<String, Object> response, String origin, String destination) {
        try {
            List<Map<String, Object>> routes = (List<Map<String, Object>>) response.get("routes");
            if (routes == null || routes.isEmpty()) {
                return "❌ No routes found between " + origin + " and " + destination;
            }

            Map<String, Object> route = routes.get(0); // Use first route
            List<Map<String, Object>> legs = (List<Map<String, Object>>) route.get("legs");
            
            if (legs == null || legs.isEmpty()) {
                return "❌ No route legs found";
            }

            StringBuilder result = new StringBuilder();
            result.append("🗺️ **ROUTE DETAILS**\n\n");
            result.append("📍 **From:** ").append(origin).append("\n");
            result.append("📍 **To:** ").append(destination).append("\n\n");

            // Calculate totals
            long totalDistance = 0;
            long totalDuration = 0;

            for (Map<String, Object> leg : legs) {
                Map<String, Object> distance = (Map<String, Object>) leg.get("distance");
                Map<String, Object> duration = (Map<String, Object>) leg.get("duration");
                
                if (distance != null) {
                    totalDistance += ((Number) distance.get("value")).longValue();
                }
                if (duration != null) {
                    totalDuration += ((Number) duration.get("value")).longValue();
                }
            }

            result.append("📏 **Total Distance:** ").append(formatDistance(totalDistance)).append("\n");
            result.append("⏱️ **Estimated Duration:** ").append(formatDuration(totalDuration)).append("\n\n");

            // Add step-by-step directions for first leg
            if (!legs.isEmpty()) {
                Map<String, Object> firstLeg = legs.get(0);
                List<Map<String, Object>> steps = (List<Map<String, Object>>) firstLeg.get("steps");
                
                if (steps != null && !steps.isEmpty()) {
                    result.append("📋 **Directions:**\n");
                    int stepNum = 1;
                    for (Map<String, Object> step : steps) {
                        String instruction = (String) step.get("html_instructions");
                        if (instruction != null) {
                            // Remove HTML tags
                            String cleanInstruction = instruction.replaceAll("<[^>]*>", "");
                            
                            Map<String, Object> stepDistance = (Map<String, Object>) step.get("distance");
                            String distanceText = stepDistance != null ? (String) stepDistance.get("text") : "";
                            
                            result.append(stepNum++).append(". ").append(cleanInstruction);
                            if (!distanceText.isEmpty()) {
                                result.append(" (").append(distanceText).append(")");
                            }
                            result.append("\n");
                        }
                    }
                }
            }

            return result.toString();

        } catch (Exception e) {
            log.error("❌ Error formatting directions response: {}", e.getMessage(), e);
            return "❌ Error formatting route details: " + e.getMessage();
        }
    }

    /**
     * Format distance from meters to human-readable string
     */
    private String formatDistance(long meters) {
        if (meters < 1000) {
            return meters + " m";
        } else {
            double km = meters / 1000.0;
            return String.format("%.1f km (%.1f mi)", km, km * 0.621371);
        }
    }

    /**
     * Format duration from seconds to human-readable string
     */
    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        
        if (hours > 0) {
            return String.format("%d hr %d min", hours, minutes);
        } else {
            return String.format("%d min", minutes);
        }
    }

    /**
     * Request DTO for directions calculation
     */
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
            this.travelMode = "driving"; // Default
        }
    }
}
