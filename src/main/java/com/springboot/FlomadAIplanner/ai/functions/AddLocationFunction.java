package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.model.entities.Location;
import com.springboot.FlomadAIplanner.service.model.LocationService;
import com.springboot.FlomadAIplanner.service.socket.WebSocketService;
import com.springboot.FlomadAIplanner.util.SessionContextHolder;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
@Component
@Description("""
        🌍 INTELLIGENT TRAVEL LOCATION BUILDER - Your Ultimate Itinerary Mapping Tool
        Strategically adds culturally significant destinations to user's visual travel map, creating comprehensive
        multi-destination itineraries with real-time map visualization. Each location becomes an interactive marker
        with cultural context, travel timing, and insider knowledge.
        
        ⚠️ CRITICAL: Verify coordinates match the intended destination city/region before adding!
        - For Paris trips: lat ≈ 48.8°, lon ≈ 2.3° | For Rome: lat ≈ 41.9°, lon ≈ 12.5°
        - For New York: lat ≈ 40.7°, lon ≈ -74.0° | For London: lat ≈ 51.5°, lon ≈ -0.1°
        - If coordinates don't match, use placeTextSearchFunction with full city context
        
        EXPERT TRAVEL INTELLIGENCE:
        - Cultural Context Analysis: Understands seasonal timing, local customs, visa requirements
        - Strategic Route Planning: Optimizes travel flow, transportation connections, cultural experiences
        - Authentic Experience Curation: Prioritizes local favorites over tourist traps
        - Seasonal Optimization: Considers weather patterns, cultural events, crowd levels
        
        PERFECT FOR COMPREHENSIVE TRAVEL PLANNING:
        ✈️ Multi-Country Adventures: "Add Paris, Rome, Barcelona, and Lisbon for European cultural immersion"
        🏛️ Cultural Discovery Routes: "Include Athens, Istanbul, Cairo for ancient civilization exploration"  
        🌸 Seasonal Experiences: "Add Japan destinations for cherry blossom season timing"
        🍷 Culinary Journeys: "Build wine country route through Tuscany, Bordeaux, and Napa Valley"
        🏖️ Progressive Island Hopping: "Map Greek islands: Santorini, Mykonos, Crete, Rhodes"
        
        TRAVEL INTELLIGENCE FEATURES:
        - Cultural Event Awareness: Considers festivals, holidays, seasonal attractions
        - Visa & Entry Requirements: Tracks travel document needs between destinations
        - Transportation Optimization: Logical routing for trains, flights, road trips
        - Local Insider Knowledge: Hidden gems, authentic experiences, cultural etiquette
        - Budget Consciousness: Considers cost variations between destinations and seasons
        
        USAGE STRATEGY vs Other Functions:
        ✅ Use AddLocationFunction: Multi-destination trips, progressive planning, cultural routes
        ❌ Don't use for: Single location exploration (use ShowLocationFunction instead)
        
        REAL-TIME EXPERIENCE:
        - Instant visual feedback with interactive map markers
        - Cultural context popups with insider travel tips
        - Optimal timing recommendations for each destination
        - Smart duplicate prevention for seamless user experience
        
        Returns: Culturally-aware confirmation with travel timing and insider tips for added destinations
        """)
public class AddLocationFunction implements Function<AddLocationFunction.AddLocationRequest, String> {
    private final LocationService locationService;
    private final WebSocketService webSocketService;
    
    // Duplicate request prevention (keep requests for 30 seconds)
    private static final ConcurrentHashMap<String, Long> processedRequests = new ConcurrentHashMap<>();
    private static final long DUPLICATE_TIMEOUT = 30000; // 30 seconds

    public AddLocationFunction(LocationService locationService, WebSocketService webSocketService) {
        this.locationService = locationService;
        this.webSocketService = webSocketService;
    }

    @Override
    public String apply(AddLocationRequest request) {
        // Prevent duplicate function calls by checking if this exact request was processed recently
        String requestHash = generateRequestHash(request);
        if (isDuplicateRequest(requestHash)) {
            return "✅ Location already processed: " + request.getName();
        }
        markRequestProcessed(requestHash);

        try {
            // Get sessionId from request first, then fall back to context
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = SessionContextHolder.getSessionId();
            }

            if (sessionId == null || sessionId.isEmpty()) {
                return "Error: Missing session ID in request and context.";
            }

            // Validate coordinates - if missing, we can't proceed with this function
            if (request.getLatitude() == null || request.getLongitude() == null) {
                if (request.getName() != null && !request.getName().isEmpty()) {
                    return String.format("Error: Coordinates required for location '%s'. Please use placeTextSearchFunction first to get coordinates.", request.getName());
                }
                return "Error: Latitude and longitude are required for addLocationFunction.";
            }

            if (request.getLatitude() < -90 || request.getLatitude() > 90) {
                return "Error: Latitude must be between -90 and 90.";
            }

            if (request.getLongitude() < -180 || request.getLongitude() > 180) {
                return "Error: Longitude must be between -180 and 180.";
            }

            // Check for duplicate locations within 100 meters
            List<Location> existingLocations = locationService.findAllBySessionId(sessionId);
            
            // Geographic sanity check: Detect if new location is suspiciously far from existing locations
            // This prevents adding Athens/New York/London locations to a Paris trip
            if (!existingLocations.isEmpty()) {
                // Calculate distance to first location as a reference point
                Location firstLocation = existingLocations.get(0);
                double distanceToFirst = calculateHaversineDistance(
                    request.getLatitude(), request.getLongitude(),
                    firstLocation.getLatitude(), firstLocation.getLongitude()
                );
                
                // If the new location is more than 500km from the first location, warn about potential error
                if (distanceToFirst > 500000) { // 500km threshold for regional trips
                    log.warn("⚠️ LOCATION VERIFICATION WARNING: '{}' at ({}, {}) is {}km from existing location '{}' at ({}, {}). This may indicate wrong coordinates!",
                        request.getName(), request.getLatitude(), request.getLongitude(),
                        Math.round(distanceToFirst / 1000),
                        firstLocation.getName(), firstLocation.getLatitude(), firstLocation.getLongitude());
                    
                    return String.format("⚠️ Location verification failed: '%s' appears to be %dkm away from '%s'. " +
                        "Are you sure these locations are part of the same trip? " +
                        "If searching for '%s', make sure to include the city name (e.g., 'RELAX Spa, Paris' not 'RELAX Spa'). " +
                        "Please verify coordinates or use placeTextSearchFunction with full city context.",
                        request.getName(), Math.round(distanceToFirst / 1000), firstLocation.getName(), request.getName());
                }
            }
            
            for (Location existing : existingLocations) {
                double distance = calculateHaversineDistance(
                    request.getLatitude(), request.getLongitude(),
                    existing.getLatitude(), existing.getLongitude()
                );
                if (distance < 100) { // 100 meters threshold
                    log.warn("⚠️ Location too close to existing location '{}' ({}m away)", existing.getName(), Math.round(distance));
                    // If it's the exact same location, return success to avoid confusion
                    if (distance < 10 && existing.getName() != null && existing.getName().equalsIgnoreCase(request.getName())) {
                        log.info("📍 Exact same location already exists, returning success: {}", existing.getName());
                        return "✅ " + existing.getName() + " is already on your map!";
                    }
                    return String.format("Location is too close to existing location '%s' (%.0fm away). " +
                        "Locations must be at least 100 meters apart.", existing.getName(), distance);
                }
            }

            // Sanitize location name to remove invalid characters
            String sanitizedName = sanitizeLocationName(request.getName());
            String sanitizedType = sanitizeLocationType(request.getType());
            
            Location location = Location.builder()
                    .sessionId(sessionId)
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .name(sanitizedName)
                    .type(sanitizedType)
                    // Rich data fields for enhanced UX
                    .photos(request.getPhotos())
                    .rating(request.getRating())
                    .userRatingsTotal(request.getUserRatingsTotal())
                    .priceLevel(request.getPriceLevel())
                    .phoneNumber(request.getPhoneNumber())
                    .website(request.getWebsite())
                    .vicinity(request.getVicinity())
                    .placeId(request.getPlaceId())
                    .isOpen(request.getIsOpen())
                    .weekdayText(request.getWeekdayText())
                    .build();

            // Save the location to the database
            locationService.save(location);
            log.info("📍 Location saved: {}", location);

            // Notify the frontend via WebSocket
            webSocketService.sendLocationUpdate(location);
            log.info("📡 WebSocket update sent for location: {}", location.getName());

            return "✅ Location added successfully: " + location.getName();
        } catch (Exception e) {
            log.error("❌ Error in AddLocationFunction: {}", e.getMessage(), e);
            return "Error adding location: " + e.getMessage();
        }
    }

    /**
     * Sanitize location name by removing invalid characters that would fail validation
     * Keeps: letters, numbers, spaces, and common punctuation: -.,()''&|?!:;/+#@$%
     * Removes: emojis, special symbols, and other invalid characters
     */
    private String sanitizeLocationName(String name) {
        if (name == null || name.isEmpty()) {
            return "Unnamed Location";
        }
        
        // Remove emojis and other special characters not in the allowed pattern
        // Pattern from LocationDTO: ^[a-zA-Z0-9\\s\\-.,()''&|?!:;/+#@$%\\u00C0-\\u024F\\u1E00-\\u1EFF]*$
        String sanitized = name.replaceAll("[^a-zA-Z0-9\\s\\-.,()''&|?!:;/+#@$%\\u00C0-\\u024F\\u1E00-\\u1EFF]", "");
        
        // Trim whitespace and limit to 200 characters
        sanitized = sanitized.trim();
        if (sanitized.length() > 200) {
            sanitized = sanitized.substring(0, 200);
        }
        
        // If sanitization removed everything, provide a default
        if (sanitized.isEmpty()) {
            return "Unnamed Location";
        }
        
        return sanitized;
    }
    
    /**
     * Sanitize location type to ensure it matches allowed values
     * Valid types: attraction, cafe, hotel, restaurant, transport, park, museum, beach, 
     * mountain, landmark, shopping, entertainment, religious, hospital, airport, 
     * station, viewpoint, nature, sports, accommodation
     */
    private String sanitizeLocationType(String type) {
        if (type == null || type.isEmpty()) {
            return "attraction";
        }
        
        // Convert to lowercase for matching
        String lowerType = type.toLowerCase().trim();
        
        // List of valid types from LocationDTO validation
        String[] validTypes = {
            "attraction", "cafe", "hotel", "restaurant", "transport", "park", 
            "museum", "beach", "mountain", "landmark", "shopping", "entertainment", 
            "religious", "hospital", "airport", "station", "viewpoint", "nature", 
            "sports", "accommodation"
        };
        
        // Check if type is already valid
        for (String validType : validTypes) {
            if (lowerType.equals(validType)) {
                return validType;
            }
        }
        
        // Try to map common variations to valid types
        if (lowerType.contains("food") || lowerType.contains("dining") || lowerType.contains("eat")) {
            return "restaurant";
        }
        if (lowerType.contains("coffee") || lowerType.contains("tea")) {
            return "cafe";
        }
        if (lowerType.contains("stay") || lowerType.contains("lodge") || lowerType.contains("inn")) {
            return "accommodation";
        }
        if (lowerType.contains("store") || lowerType.contains("shop") || lowerType.contains("market")) {
            return "shopping";
        }
        if (lowerType.contains("church") || lowerType.contains("temple") || lowerType.contains("mosque")) {
            return "religious";
        }
        if (lowerType.contains("train") || lowerType.contains("bus") || lowerType.contains("metro")) {
            return "station";
        }
        if (lowerType.contains("view") || lowerType.contains("lookout") || lowerType.contains("scenic")) {
            return "viewpoint";
        }
        
        // Default to "attraction" if no match
        log.warn("⚠️ Unknown location type '{}', defaulting to 'attraction'", type);
        return "attraction";
    }
    
    /**
     * Calculate the distance between two coordinates using the Haversine formula
     * @param lat1 Latitude of first point
     * @param lng1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lng2 Longitude of second point
     * @return Distance in meters
     */
    private double calculateHaversineDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371000; // Earth's radius in meters
        final double φ1 = Math.toRadians(lat1);
        final double φ2 = Math.toRadians(lat2);
        final double Δφ = Math.toRadians(lat2 - lat1);
        final double Δλ = Math.toRadians(lng2 - lng1);

        final double a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
                Math.cos(φ1) * Math.cos(φ2) *
                Math.sin(Δλ/2) * Math.sin(Δλ/2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c; // Distance in meters
    }

    /**
     * Generate a hash for duplicate detection
     */
    private String generateRequestHash(AddLocationRequest request) {
        return String.format("%s_%f_%f_%s", 
            SessionContextHolder.getSessionId(),
            request.getLatitude(), 
            request.getLongitude(),
            request.getName() != null ? request.getName() : "unnamed");
    }

    /**
     * Check if this request was processed recently
     */
    private boolean isDuplicateRequest(String requestHash) {
        cleanupOldRequests();
        Long lastProcessed = processedRequests.get(requestHash);
        if (lastProcessed != null) {
            return (System.currentTimeMillis() - lastProcessed) < DUPLICATE_TIMEOUT;
        }
        return false;
    }

    /**
     * Mark a request as processed
     */
    private void markRequestProcessed(String requestHash) {
        processedRequests.put(requestHash, System.currentTimeMillis());
    }

    /**
     * Clean up old processed requests to prevent memory leak
     */
    private void cleanupOldRequests() {
        long currentTime = System.currentTimeMillis();
        processedRequests.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > DUPLICATE_TIMEOUT);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonClassDescription("Request to add a new location to the map. JSON Schema of type: 'object'")
    public static class AddLocationRequest {

        @NotNull(message = "Latitude cannot be null.")
        @JsonPropertyDescription("Required. Latitude of the location.")
        private Double latitude;

        @NotNull(message = "Longitude cannot be null.")
        @JsonPropertyDescription("Required. Longitude of the location.")
        private Double longitude;

        @JsonPropertyDescription("Optional. Name or description of the location.")
        private String name;

        @JsonPropertyDescription("Optional. Type of location (attraction, cafe, hotel, restaurant, transport, park, museum, beach, mountain, landmark, shopping, entertainment, religious, hospital, airport, station, viewpoint, nature, sports, accommodation). Defaults to 'attraction' if not specified.")
        private String type;
        
        @JsonPropertyDescription("Optional. The ID of the chat session this location belongs to. If not provided, will be retrieved from context.")
        private String sessionId;

        // Enhanced rich data fields for better UX
        @JsonPropertyDescription("Optional. List of photo URLs from Google Places API.")
        private java.util.List<String> photos;

        @JsonPropertyDescription("Optional. Google Places rating (0-5 scale).")
        private Double rating;

        @JsonPropertyDescription("Optional. Total number of user ratings.")
        private Integer userRatingsTotal;

        @JsonPropertyDescription("Optional. Google Places price level (1-4 scale).")
        private Integer priceLevel;

        @JsonPropertyDescription("Optional. Business phone number.")
        private String phoneNumber;

        @JsonPropertyDescription("Optional. Business website URL.")
        private String website;

        @JsonPropertyDescription("Optional. Business address or vicinity.")
        private String vicinity;

        @JsonPropertyDescription("Optional. Google Places ID for detailed lookups.")
        private String placeId;

        @JsonPropertyDescription("Optional. Current open status.")
        private Boolean isOpen;

        @JsonPropertyDescription("Optional. Opening hours text.")
        private java.util.List<String> weekdayText;

        // Constructors for backward compatibility
        public AddLocationRequest(Double latitude, Double longitude, String name, String type, String sessionId) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.name = name;
            this.type = type;
            this.sessionId = sessionId;
        }
    }
}
