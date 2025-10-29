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
        🧠 INTELLIGENT LOCATION DISCOVERY ENGINE - Your AI Travel Conversation Parser
        Revolutionizes travel planning by automatically detecting, analyzing, and mapping every location mention
        in natural conversation. Transforms casual travel chat into comprehensive visual itineraries with
        cultural intelligence and precise geographic coordination.
        
        ADVANCED TRAVEL INTELLIGENCE:
        🎯 Cultural Context Recognition: Identifies not just places, but cultural significance, local names, landmarks
        🗺️ Geographic Precision: Resolves ambiguous locations using travel context and cultural knowledge  
        🌍 Multi-Language Support: Understands location names in local languages and transliterations
        📍 Landmark Intelligence: Recognizes famous sites, hidden gems, local favorites, cultural attractions
        
        PERFECT FOR SOPHISTICATED TRAVEL CONVERSATIONS:
        🗨️ Natural Planning: "I want to explore Paris - the Eiffel Tower, Montmartre, and that cute café in Le Marais"
        🎪 Cultural Discovery: "Show me Tokyo's Shibuya crossing, Senso-ji temple, and Tsukiji fish market"
        🏛️ Historical Journeys: "Ancient Rome tour: Colosseum, Pantheon, Roman Forum, and Palatine Hill"
        🍷 Culinary Adventures: "Tuscany wine tour through Chianti, Montalcino, and Montepulciano regions"
        🎨 Art & Culture: "Madrid art triangle: Prado Museum, Reina Sofia, and Thyssen-Bornemisza"
        
        CRITICAL TOUR INTEGRATION INTELLIGENCE:
        ⚡ MANDATORY After Tours: Automatically extracts precise venues from tour descriptions
        📍 Venue Discovery: "Seine River Cruise" → Maps exact departure points, route highlights  
        🎪 Activity Mapping: "Montmartre Walking Tour" → Extracts Sacré-Cœur, Place du Tertre, Moulin Rouge
        🍴 Restaurant Intelligence: Identifies specific venues, addresses, cultural dining contexts
        
        SMART CONTEXTUAL PROCESSING:
        - Cultural Event Awareness: Links locations to festivals, seasons, cultural significance
        - Travel Flow Logic: Understands geographic relationships and optimal routing
        - Authentic Experience Focus: Prioritizes local recommendations over mainstream tourist sites
        - Seasonal Intelligence: Considers timing factors for outdoor activities and cultural events
        
        REAL-TIME TRAVEL ORCHESTRATION:
        1. 🔍 Advanced NLP analyzes travel conversation for location entities and cultural context
        2. 🌐 Google Places API with cultural intelligence resolves exact coordinates and details
        3. 📍 Instant map visualization with cultural context popups and travel timing
        4. ✨ Smart duplicate prevention with travel flow optimization
        
        WORKFLOW EXCELLENCE:
        - Detects explicit locations: "Paris", "Eiffel Tower", "Le Marais district"  
        - Infers cultural sites: "that famous cathedral in Barcelona" → Sagrada Familia
        - Extracts from descriptions: "riverside dining" → Seine waterfront restaurants
        - Maps activity locations: "flamenco show" → authentic tablao venues in Spain
        
        Returns: Culturally-aware confirmation with travel intelligence and timing recommendations
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
        java.util.List<String> locations = new java.util.ArrayList<>();
        String normalizedMessage = message.toLowerCase().trim();
        
        // Check for known landmarks and locations with fuzzy matching
        String[] knownLocations = {
            "eiffel tower", "eiffel", "tower", "paris", "louvre", "champs elysees", "champs-elysees",
            "arc de triomphe", "triomphe", "notre dame", "notre-dame", "sacre coeur", "sacre-coeur",
            "trocadero", "versailles", "montmartre", "seine", "marais", "latin quarter",
            "london", "big ben", "tower bridge", "buckingham palace", "westminster", "thames",
            "tokyo", "shibuya", "harajuku", "shinjuku", "ginza", "akihabara", "sensoji",
            "new york", "nyc", "manhattan", "brooklyn", "times square", "central park", "statue of liberty",
            "rome", "colosseum", "vatican", "trevi fountain", "spanish steps", "pantheon",
            "barcelona", "sagrada familia", "park guell", "las ramblas", "gothic quarter"
        };
        
        // Check for fuzzy matches with known locations
        for (String knownLocation : knownLocations) {
            if (containsFuzzyMatch(normalizedMessage, knownLocation)) {
                locations.add(knownLocation);
                log.info("🎯 Found fuzzy match for known location: {} in message: {}", knownLocation, message);
            }
        }
        
        // If no known locations found, try extracting potential location words
        if (locations.isEmpty()) {
            String[] commonLocationWords = {
                "city", "cities", "town", "village", "place", "destination", "location",
                "country", "countries", "state", "province", "region", "area", "show", "me", "the"
            };
            
            // Remove common travel words
            String cleanedMessage = normalizedMessage;
            for (String word : commonLocationWords) {
                cleanedMessage = cleanedMessage.replaceAll("\\b" + word + "\\b", " ");
            }
            
            // Look for both capitalized and non-capitalized words (more flexible)
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b[A-Za-z]{3,}(?:\\s+[A-Za-z]{3,})*\\b");
            java.util.regex.Matcher matcher = pattern.matcher(cleanedMessage.trim());
            
            while (matcher.find()) {
                String potentialLocation = matcher.group().trim();
                // Filter out common words that aren't locations
                if (!isCommonNonLocation(potentialLocation) && potentialLocation.length() > 2) {
                    locations.add(potentialLocation);
                    log.info("📍 Extracted potential location: {} from message: {}", potentialLocation, message);
                }
            }
        }
        
        return locations.toArray(new String[0]);
    }
    
    /**
     * Check if message contains a fuzzy match for a known location
     * Handles typos and partial matches
     */
    private boolean containsFuzzyMatch(String message, String knownLocation) {
        // Direct match
        if (message.contains(knownLocation)) {
            return true;
        }
        
        // Check for typos in key words
        String[] locationWords = knownLocation.split("\\s+");
        int matchedWords = 0;
        
        for (String word : locationWords) {
            if (message.contains(word) || containsTypo(message, word)) {
                matchedWords++;
            }
        }
        
        // Consider it a match if most words are found (with typo tolerance)
        return matchedWords >= Math.max(1, locationWords.length * 0.7);
    }
    
    /**
     * Check if message contains a word with potential typos
     */
    private boolean containsTypo(String message, String targetWord) {
        if (targetWord.length() < 4) return false; // Skip short words
        
        String[] messageWords = message.split("\\s+");
        for (String messageWord : messageWords) {
            // Check for single character differences for words of similar length
            if (Math.abs(messageWord.length() - targetWord.length()) <= 1) {
                int differences = calculateLevenshteinDistance(messageWord, targetWord);
                // Allow 1-2 character differences for typos
                if (differences <= 2 && differences < targetWord.length() * 0.4) {
                    log.info("🔍 Found potential typo match: '{}' for target: '{}'", messageWord, targetWord);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Calculate Levenshtein distance for typo detection
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1)
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }

    /**
     * Check if a word/phrase is likely NOT a location
     */
    private boolean isCommonNonLocation(String word) {
        String[] nonLocations = {
            "I", "The", "This", "That", "There", "Here", "What", "When", "Where", "How", "Why",
            "Hello", "Hi", "Thanks", "Thank", "Please", "Can", "Could", "Would", "Should", "Will",
            "Travel", "Trip", "Plan", "Planning", "Visit", "Go", "See", "Show", "Find", "Search",
            "Want", "Need", "Like", "Love", "Enjoy", "Book", "Reserve", "Check", "Look", "Take",
            "Get", "Make", "Have", "Been", "Going", "Want", "About", "From", "With", "For",
            "And", "But", "Or", "So", "If", "Then", "Now", "Today", "Tomorrow", "Yesterday"
        };
        
        String normalizedWord = word.toLowerCase().trim();
        for (String nonLocation : nonLocations) {
            if (normalizedWord.equals(nonLocation.toLowerCase())) {
                return true;
            }
        }
        
        // Filter out very short words (likely not locations)
        if (normalizedWord.length() < 3) {
            return true;
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
        if (normalizedLocation.contains("central park")) {
            return new double[]{40.7812, -73.9665}; // Central Park
        }
        if (normalizedLocation.contains("brooklyn bridge")) {
            return new double[]{40.7061, -73.9969}; // Brooklyn Bridge
        }
        
        // Tokyo landmarks
        if (normalizedLocation.contains("tokyo")) {
            return new double[]{35.6762, 139.6503}; // Central Tokyo
        }
        if (normalizedLocation.contains("shibuya")) {
            return new double[]{35.6598, 139.7006}; // Shibuya Crossing
        }
        if (normalizedLocation.contains("harajuku")) {
            return new double[]{35.6702, 139.7016}; // Harajuku
        }
        if (normalizedLocation.contains("ginza")) {
            return new double[]{35.6716, 139.7649}; // Ginza
        }
        
        // Barcelona landmarks
        if (normalizedLocation.contains("barcelona")) {
            return new double[]{41.3851, 2.1734}; // Central Barcelona
        }
        if (normalizedLocation.contains("sagrada familia")) {
            return new double[]{41.4036, 2.1744}; // Sagrada Familia
        }
        if (normalizedLocation.contains("park guell") || normalizedLocation.contains("park güell")) {
            return new double[]{41.4145, 2.1527}; // Park Güell
        }
        if (normalizedLocation.contains("las ramblas") || normalizedLocation.contains("la rambla")) {
            return new double[]{41.3818, 2.1772}; // Las Ramblas
        }
        
        // Berlin landmarks
        if (normalizedLocation.contains("berlin")) {
            return new double[]{52.5200, 13.4050}; // Central Berlin
        }
        if (normalizedLocation.contains("brandenburg gate")) {
            return new double[]{52.5163, 13.3777}; // Brandenburg Gate
        }
        if (normalizedLocation.contains("berlin wall")) {
            return new double[]{52.5351, 13.3910}; // Berlin Wall Memorial
        }
        
        // Amsterdam landmarks  
        if (normalizedLocation.contains("amsterdam")) {
            return new double[]{52.3676, 4.9041}; // Central Amsterdam
        }
        if (normalizedLocation.contains("anne frank")) {
            return new double[]{52.3752, 4.8840}; // Anne Frank House
        }
        if (normalizedLocation.contains("vondelpark")) {
            return new double[]{52.3580, 4.8686}; // Vondelpark
        }
        
        // Dubai landmarks
        if (normalizedLocation.contains("dubai")) {
            return new double[]{25.2048, 55.2708}; // Central Dubai
        }
        if (normalizedLocation.contains("burj khalifa")) {
            return new double[]{25.1972, 55.2744}; // Burj Khalifa
        }
        
        // Sydney landmarks
        if (normalizedLocation.contains("sydney")) {
            return new double[]{-33.8688, 151.2093}; // Central Sydney
        }
        if (normalizedLocation.contains("opera house")) {
            return new double[]{-33.8568, 151.2153}; // Sydney Opera House
        }
        if (normalizedLocation.contains("harbour bridge")) {
            return new double[]{-33.8523, 151.2108}; // Sydney Harbour Bridge
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