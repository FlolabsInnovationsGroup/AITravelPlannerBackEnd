package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import com.springboot.FlomadAIplanner.service.google.GooglePlacesServiceAsync;
import com.springboot.FlomadAIplanner.util.SessionContextHolder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
@Description("""
        Automatically extracts and displays locations mentioned in user messages. When users mention places 
        like "Paris", "Eiffel Tower", "Times Square", etc., this function intelligently identifies them, 
        gets their coordinates, and adds them to the map for immediate visualization.
        
        CORE PURPOSE: Seamless location discovery - turn natural language into map visualizations
        
        PERFECT FOR:
        - Natural conversation: "I want to visit Paris and see the Eiffel Tower"
        - Travel planning: "Show me Tokyo, specifically Shibuya and Harajuku"
        - Location discovery: "What about Barcelona and its famous landmarks?"
        - Exploration queries: "I'm interested in New York, Rome, and London"
        - TOUR RESULTS: "Seine River Cruise, Montmartre Walking Tour, Eiffel Tower visit"
        - ACTIVITY MENTIONS: Extract specific locations from tour descriptions and activity recommendations
        
        SMART FEATURES:
        - Natural language processing for location detection
        - Automatic coordinate resolution via Google Places
        - Real-time map updates via WebSocket
        - Smart handling of ambiguous locations
        - Duplicate prevention for existing locations
        
        WORKFLOW:
        1. Detects location mentions in user text
        2. Searches Google Places for accurate coordinates
        3. Automatically adds locations to user's map
        4. Provides confirmation of locations found and added
        
        CRITICAL FOR TOURS: Always call after toursFreeTextSearchFunction to extract specific tour locations 
        like "Seine River", "Montmartre", "Central Park" from tour descriptions and add them to user's map.
        
        Returns: Confirmation of locations extracted and added to map
        """)
public class AutoLocationExtractionFunction implements Function<AutoLocationExtractionFunction.LocationExtractionRequest, String> {
    
    private final GooglePlacesServiceAsync googlePlacesService;
    private final AddLocationFunction addLocationFunction;

    public AutoLocationExtractionFunction(GooglePlacesServiceAsync googlePlacesService, 
                                        AddLocationFunction addLocationFunction) {
        this.googlePlacesService = googlePlacesService;
        this.addLocationFunction = addLocationFunction;
    }

    @Override
    public String apply(LocationExtractionRequest request) {
        log.info("🔍 Starting AutoLocationExtractionFunction for message: {}", request.getMessage());

        try {
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = SessionContextHolder.getSessionId();
            }

            if (sessionId == null || sessionId.isEmpty()) {
                log.error("❌ Session ID is missing. Cannot proceed with location extraction.");
                return "Error: Missing session ID.";
            }

            // Extract location names from the message
            String[] potentialLocations = extractLocationNames(request.getMessage());
            
            if (potentialLocations.length == 0) {
                log.info("ℹ️ No clear location names found in message");
                return "No specific locations were clearly identified in your message. Try mentioning specific city names, landmarks, or attractions.";
            }

            StringBuilder result = new StringBuilder();
            int addedCount = 0;
            int failedCount = 0;

            for (String locationName : potentialLocations) {
                try {
                    log.info("📍 Processing location: {}", locationName);
                    
                    double latitude = 0;
                    double longitude = 0;
                    String displayName = locationName;
                    String locationType = "tourist_attraction";
                    boolean foundCoordinates = false;
                    
                    try {
                        // First try Google Places API
                        TextSearchRequest searchRequest = new TextSearchRequest();
                        searchRequest.setTextQuery(locationName);
                        
                        var searchResult = googlePlacesService.asyncTextSearchPlaces(searchRequest).join();
                        
                        if (searchResult != null && searchResult.has("places") && 
                            searchResult.get("places").isArray() && 
                            searchResult.get("places").size() > 0) {
                            
                            var place = searchResult.get("places").get(0);
                            var location = place.get("location");
                            
                            if (location != null && location.has("latitude") && location.has("longitude")) {
                                latitude = location.get("latitude").asDouble();
                                longitude = location.get("longitude").asDouble();
                                displayName = place.has("displayName") && place.get("displayName").has("text") 
                                    ? place.get("displayName").get("text").asText() 
                                    : locationName;
                                locationType = determineLocationType(place);
                                foundCoordinates = true;
                                log.info("✅ Found coordinates via Google Places API: {} ({}, {})", displayName, latitude, longitude);
                            }
                        }
                    } catch (Exception apiException) {
                        log.warn("⚠️ Google Places API failed for {}: {}", locationName, apiException.getMessage());
                    }
                    
                    // If Google Places API failed, try fallback coordinates
                    if (!foundCoordinates) {
                        var fallbackCoords = getFallbackCoordinates(locationName);
                        if (fallbackCoords != null) {
                            latitude = fallbackCoords[0];
                            longitude = fallbackCoords[1];
                            displayName = locationName;
                            locationType = "tourist_attraction";
                            foundCoordinates = true;
                            log.info("✅ Using fallback coordinates for {}: ({}, {})", displayName, latitude, longitude);
                        }
                    }
                    
                    if (foundCoordinates) {
                        // Create AddLocationRequest
                        AddLocationFunction.AddLocationRequest addRequest = 
                            new AddLocationFunction.AddLocationRequest(
                                latitude, longitude, displayName, locationType, sessionId);
                        
                        // Add the location using AddLocationFunction
                        String addResult = addLocationFunction.apply(addRequest);
                        
                        if (addResult.startsWith("✅")) {
                            addedCount++;
                            result.append("✅ ").append(displayName).append(" added to map\n");
                            log.info("✅ Successfully added location: {}", displayName);
                        } else {
                            failedCount++;
                            result.append("⚠️ ").append(displayName).append(": ").append(addResult).append("\n");
                            log.warn("⚠️ Failed to add location {}: {}", displayName, addResult);
                        }
                    } else {
                        failedCount++;
                        result.append("❌ Could not find coordinates for: ").append(locationName).append("\n");
                        log.warn("❌ No coordinates found for: {}", locationName);
                    }
                } catch (Exception e) {
                    failedCount++;
                    result.append("❌ Error processing: ").append(locationName).append("\n");
                    log.error("❌ Error processing location {}: {}", locationName, e.getMessage());
                }
            }

            // Prepare final summary
            String summary = String.format("🗺️ Location extraction complete: %d added, %d failed", addedCount, failedCount);
            log.info(summary);
            
            return summary + "\n\n" + result.toString().trim();

        } catch (Exception e) {
            log.error("❌ Error in AutoLocationExtractionFunction: {}", e.getMessage(), e);
            return "Error extracting locations: " + e.getMessage();
        }
    }

    /**
     * Extract potential location names from user message
     */
    private String[] extractLocationNames(String message) {
        // Simple approach: look for capitalized words/phrases that could be locations
        // This could be enhanced with NLP libraries for better location detection
        String[] commonLocationWords = {
            "city", "cities", "town", "village", "place", "destination", "location",
            "country", "countries", "state", "province", "region", "area"
        };
        
        // Remove common travel words and extract potential location names
        String cleanedMessage = message.toLowerCase();
        for (String word : commonLocationWords) {
            cleanedMessage = cleanedMessage.replaceAll("\\b" + word + "\\b", "");
        }
        
        // Look for capitalized words in original message (potential proper nouns/locations)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b[A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*\\b");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        
        java.util.List<String> locations = new java.util.ArrayList<>();
        while (matcher.find()) {
            String potentialLocation = matcher.group().trim();
            // Filter out common words that aren't locations
            if (!isCommonNonLocation(potentialLocation)) {
                locations.add(potentialLocation);
            }
        }
        
        return locations.toArray(new String[0]);
    }

    /**
     * Check if a capitalized word/phrase is likely NOT a location
     */
    private boolean isCommonNonLocation(String word) {
        String[] nonLocations = {
            "I", "The", "This", "That", "There", "Here", "What", "When", "Where", "How", "Why",
            "Hello", "Hi", "Thanks", "Thank", "Please", "Can", "Could", "Would", "Should",
            "Travel", "Trip", "Plan", "Planning", "Visit", "Go", "See", "Show", "Find"
        };
        
        for (String nonLocation : nonLocations) {
            if (word.equalsIgnoreCase(nonLocation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine location type from Google Places response
     */
    private String determineLocationType(com.fasterxml.jackson.databind.JsonNode place) {
        if (place.has("types") && place.get("types").isArray()) {
            var types = place.get("types");
            
            // Map Google Places types to our location types
            for (int i = 0; i < types.size(); i++) {
                String type = types.get(i).asText();
                switch (type) {
                    case "lodging": return "hotel";
                    case "restaurant": case "meal_takeaway": case "food": return "restaurant";
                    case "cafe": return "cafe";
                    case "tourist_attraction": return "attraction";
                    case "museum": return "museum";
                    case "park": return "park";
                    case "airport": return "airport";
                    case "train_station": case "subway_station": case "bus_station": return "station";
                    case "shopping_mall": case "store": return "shopping";
                    case "hospital": return "hospital";
                    case "church": case "mosque": case "synagogue": return "religious";
                    case "stadium": case "gym": return "sports";
                    case "natural_feature": return "nature";
                    case "establishment": case "point_of_interest": return "landmark";
                }
            }
        }
        return "attraction"; // default
    }

    /**
     * Get fallback coordinates for common locations when Google Places API fails
     */
    private double[] getFallbackCoordinates(String locationName) {
        String normalizedLocation = locationName.toLowerCase().trim();
        
        // Paris landmarks and attractions
        if (normalizedLocation.contains("seine") || normalizedLocation.contains("river cruise")) {
            return new double[]{48.8566, 2.3522}; // Seine River (near Notre Dame)
        }
        if (normalizedLocation.contains("montmartre")) {
            return new double[]{48.8866, 2.3431}; // Montmartre
        }
        if (normalizedLocation.contains("eiffel") || normalizedLocation.contains("tower")) {
            return new double[]{48.8584, 2.2945}; // Eiffel Tower
        }
        if (normalizedLocation.contains("louvre")) {
            return new double[]{48.8606, 2.3376}; // Louvre Museum
        }
        if (normalizedLocation.contains("champs") || normalizedLocation.contains("elysees")) {
            return new double[]{48.8698, 2.3075}; // Champs-Élysées
        }
        if (normalizedLocation.contains("arc de triomphe") || normalizedLocation.contains("triomphe")) {
            return new double[]{48.8738, 2.2950}; // Arc de Triomphe
        }
        if (normalizedLocation.contains("notre dame") || normalizedLocation.contains("notre-dame")) {
            return new double[]{48.8530, 2.3499}; // Notre Dame Cathedral
        }
        if (normalizedLocation.contains("sacre") || normalizedLocation.contains("coeur")) {
            return new double[]{48.8867, 2.3431}; // Sacré-Cœur
        }
        if (normalizedLocation.contains("trocadero")) {
            return new double[]{48.8620, 2.2877}; // Trocadéro
        }
        if (normalizedLocation.contains("versailles")) {
            return new double[]{48.8049, 2.1204}; // Palace of Versailles
        }
        if (normalizedLocation.contains("latin quarter")) {
            return new double[]{48.8508, 2.3444}; // Latin Quarter
        }
        if (normalizedLocation.contains("marais")) {
            return new double[]{48.8566, 2.3622}; // Le Marais
        }
        if (normalizedLocation.contains("orly") || normalizedLocation.contains("airport")) {
            return new double[]{48.7233, 2.3794}; // Paris Orly Airport
        }
        
        // General Paris fallback
        if (normalizedLocation.contains("paris")) {
            return new double[]{48.8566, 2.3522}; // Central Paris
        }
        
        // London landmarks
        if (normalizedLocation.contains("london")) {
            return new double[]{51.5074, -0.1278}; // Central London
        }
        if (normalizedLocation.contains("big ben") || normalizedLocation.contains("westminster")) {
            return new double[]{51.4994, -0.1245}; // Big Ben/Westminster
        }
        if (normalizedLocation.contains("tower bridge")) {
            return new double[]{51.5055, -0.0754}; // Tower Bridge
        }
        if (normalizedLocation.contains("buckingham")) {
            return new double[]{51.5014, -0.1419}; // Buckingham Palace
        }
        
        // Rome landmarks
        if (normalizedLocation.contains("rome") || normalizedLocation.contains("roma")) {
            return new double[]{41.9028, 12.4964}; // Central Rome
        }
        if (normalizedLocation.contains("colosseum")) {
            return new double[]{41.8902, 12.4922}; // Colosseum
        }
        if (normalizedLocation.contains("vatican")) {
            return new double[]{41.9029, 12.4534}; // Vatican City
        }
        
        // New York landmarks
        if (normalizedLocation.contains("new york") || normalizedLocation.contains("nyc")) {
            return new double[]{40.7128, -74.0060}; // Central New York
        }
        if (normalizedLocation.contains("statue of liberty")) {
            return new double[]{40.6892, -74.0445}; // Statue of Liberty
        }
        if (normalizedLocation.contains("times square")) {
            return new double[]{40.7580, -73.9855}; // Times Square
        }
        
        // Tokyo landmarks
        if (normalizedLocation.contains("tokyo")) {
            return new double[]{35.6762, 139.6503}; // Central Tokyo
        }
        
        return null; // No fallback found
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonClassDescription("Request to automatically extract and add locations from user message text")
    public static class LocationExtractionRequest {

        @JsonPropertyDescription("Required. The user message containing potential location references")
        private String message;

        @JsonPropertyDescription("Optional. The session ID for the chat session. If not provided, will be retrieved from context.")
        private String sessionId;
    }
}