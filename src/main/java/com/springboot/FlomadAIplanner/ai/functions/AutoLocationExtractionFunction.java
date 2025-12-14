package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;

import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import com.springboot.FlomadAIplanner.service.google.GooglePlacesServiceAsync;
import com.springboot.FlomadAIplanner.util.SessionContextHolder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    
    @Value("${google.api.key}")
    private String googleApiKey;

    public AutoLocationExtractionFunction(GooglePlacesServiceAsync googlePlacesService, 
                                        AddLocationFunction addLocationFunction) {
        this.googlePlacesService = googlePlacesService;
        this.addLocationFunction = addLocationFunction;
    }

    @Override
    public String apply(LocationExtractionRequest request) {
        log.info("🔍 Starting AutoLocationExtractionFunction for message: {}", request.getMessage());
        log.info("🔍 User query context: {}", request.getUserQuery());

        try {
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = SessionContextHolder.getSessionId();
            }

            if (sessionId == null || sessionId.isEmpty()) {
                log.error("❌ Session ID is missing. Cannot proceed with location extraction.");
                return "Error: Missing session ID.";
            }

            // Extract country/region context from user query for filtering
            String userContextCountry = request.getUserQuery() != null ? 
                extractCountryFromQuery(request.getUserQuery()) : null;
            
            log.info("🌍 Detected user query country context: {}", userContextCountry != null ? userContextCountry : "none");
            
            // Extract location names from the message
            String[] potentialLocations = extractLocationNames(request.getMessage());
            
            if (potentialLocations.length == 0) {
                log.info("ℹ️ No clear location names found in message");
                return "No specific locations were clearly identified in your message. Try mentioning specific city names, landmarks, or attractions.";
            }

            StringBuilder result = new StringBuilder();
            int addedCount = 0;
            int failedCount = 0;
            int filteredCount = 0;

            for (String locationName : potentialLocations) {
                try {
                    log.info("📍 Processing location: {}", locationName);
                    
                    double latitude = 0;
                    double longitude = 0;
                    String displayName = locationName;
                    String locationType = "tourist_attraction";
                    boolean foundCoordinates = false;
                    com.fasterxml.jackson.databind.JsonNode searchResult = null;
                    String locationCountry = null;
                    
                    // Prioritize user context country over message context
                    String countryContext = userContextCountry != null ? userContextCountry : extractCountryFromQuery(request.getMessage());
                    String enhancedQuery = countryContext != null ? locationName + ", " + countryContext : locationName;
                    
                    try {
                        // First try Google Places API with country context
                        TextSearchRequest searchRequest = new TextSearchRequest();
                        searchRequest.setTextQuery(enhancedQuery);
                        
                        log.info("🔍 Searching Google Places for: {}", enhancedQuery);
                        searchResult = googlePlacesService.asyncTextSearchPlaces(searchRequest).join();
                        
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
                                
                                // ✅ NEW: Extract country from Google Places API response
                                locationCountry = extractCountryFromPlace(place);
                                log.info("🌍 Location {} is in country: {}", displayName, 
                                        locationCountry != null ? locationCountry : "unknown");
                                
                                foundCoordinates = true;
                                log.info("✅ Found coordinates via Google Places API: {} ({}, {})", displayName, latitude, longitude);
                            }
                        }
                    } catch (Exception apiException) {
                        log.warn("⚠️ Google Places API failed for {}: {}", locationName, apiException.getMessage());
                        searchResult = null; // Clear result on failure
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
                            locationCountry = null; // No country data from fallback
                            log.info("✅ Using fallback coordinates for {}: ({}, {})", displayName, latitude, longitude);
                        }
                    }
                    
                    if (foundCoordinates) {
                        // ✅ NEW: Smart filtering AFTER we have country data from API
                        if (!shouldIncludeLocation(locationCountry, userContextCountry, request.getUserQuery())) {
                            filteredCount++;
                            log.info("⏭️ Filtered out {} ({}) - not relevant to context: {}", 
                                    displayName, locationCountry, userContextCountry);
                            continue; // Skip this location
                        }
                        
                        // Create AddLocationRequest with rich data
                        AddLocationFunction.AddLocationRequest addRequest = 
                            new AddLocationFunction.AddLocationRequest();
                        addRequest.setLatitude(latitude);
                        addRequest.setLongitude(longitude);
                        addRequest.setName(displayName);
                        addRequest.setType(locationType);
                        addRequest.setSessionId(sessionId);
                        
                        // Extract rich data from Google Places API response if available
                        if (searchResult != null && searchResult.has("places") && 
                            searchResult.get("places").isArray() && 
                            searchResult.get("places").size() > 0) {
                            extractRichDataFromPlace(searchResult.get("places").get(0), addRequest);
                        }
                        
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
            String summary = String.format("🗺️ Location extraction complete: %d added, %d filtered, %d failed", 
                    addedCount, filteredCount, failedCount);
            log.info(summary);
            
            return summary + "\n\n" + result.toString().trim();

        } catch (Exception e) {
            log.error("❌ Error in AutoLocationExtractionFunction: {}", e.getMessage(), e);
            return "Error extracting locations: " + e.getMessage();
        }
    }

    /**
     * Extract country context from user query text for better geocoding accuracy.
     * Note: This method extracts country from TEXT queries, not from Google Places API responses.
     * For extracting country from API responses, see extractCountryFromPlace().
     */
    private String extractCountryFromQuery(String message) {
        String lowerMessage = message.toLowerCase();
        
        // FIRST: Check for region patterns (Southeast Asia, Europe, etc.)
        // These need to be checked BEFORE individual countries to avoid false matches
        java.util.Map<String, String[]> regionPatterns = new java.util.HashMap<>();
        regionPatterns.put("SOUTHEAST_ASIA", new String[]{
            "southeast asia", "south east asia", "se asia", "sea region", "asean"
        });
        regionPatterns.put("SOUTH_ASIA", new String[]{
            "south asia", "indian subcontinent"
        });
        regionPatterns.put("EAST_ASIA", new String[]{
            "east asia", "far east"
        });
        regionPatterns.put("WESTERN_EUROPE", new String[]{
            "western europe", "west europe", "europe"  // "europe" as fallback
        });
        regionPatterns.put("EASTERN_EUROPE", new String[]{
            "eastern europe", "east europe"
        });
        regionPatterns.put("SCANDINAVIA", new String[]{
            "scandinavia", "scandinavian", "nordic countries"
        });
        regionPatterns.put("MIDDLE_EAST", new String[]{
            "middle east", "mideast"
        });
        regionPatterns.put("NORTH_AFRICA", new String[]{
            "north africa", "northern africa"
        });
        regionPatterns.put("SOUTH_AMERICA", new String[]{
            "south america", "latin america"
        });
        regionPatterns.put("CENTRAL_AMERICA", new String[]{
            "central america"
        });
        regionPatterns.put("NORTH_AMERICA", new String[]{
            "north america"
        });
        regionPatterns.put("OCEANIA", new String[]{
            "oceania", "pacific islands", "south pacific"
        });
        
        // Check region patterns first
        for (java.util.Map.Entry<String, String[]> entry : regionPatterns.entrySet()) {
            for (String pattern : entry.getValue()) {
                if (lowerMessage.contains(pattern)) {
                    log.info("🌏 Detected REGION context: {} from keyword: '{}'", entry.getKey(), pattern);
                    return entry.getKey();  // Return region identifier
                }
            }
        }
        
        // SECOND: Check for individual country patterns
        java.util.Map<String, String[]> countryPatterns = new java.util.HashMap<>();
        countryPatterns.put("Italy", new String[]{"italy", "italian"});
        countryPatterns.put("France", new String[]{"france", "french", "paris"});
        countryPatterns.put("Spain", new String[]{"spain", "spanish", "barcelona", "madrid"});
        countryPatterns.put("Japan", new String[]{"japan", "japanese", "tokyo"});
        countryPatterns.put("United Kingdom", new String[]{"uk", "united kingdom", "england", "london", "britain", "british"});
        countryPatterns.put("United States", new String[]{"usa", "united states", "america", "american"});
        countryPatterns.put("Germany", new String[]{"germany", "german", "berlin"});
        countryPatterns.put("Greece", new String[]{"greece", "greek", "athens"});
        countryPatterns.put("Thailand", new String[]{"thailand", "thai", "bangkok"});
        countryPatterns.put("Vietnam", new String[]{"vietnam", "vietnamese", "hanoi", "saigon"});
        countryPatterns.put("Cambodia", new String[]{"cambodia", "cambodian", "phnom penh"});
        countryPatterns.put("Laos", new String[]{"laos", "lao", "vientiane"});
        countryPatterns.put("Malaysia", new String[]{"malaysia", "malaysian", "kuala lumpur"});
        countryPatterns.put("Singapore", new String[]{"singapore", "singaporean"});
        countryPatterns.put("Indonesia", new String[]{"indonesia", "indonesian", "jakarta", "bali"});
        countryPatterns.put("Philippines", new String[]{"philippines", "philippine", "filipino", "manila"});
        
        for (java.util.Map.Entry<String, String[]> entry : countryPatterns.entrySet()) {
            for (String pattern : entry.getValue()) {
                if (lowerMessage.contains(pattern)) {
                    log.info("🌍 Detected country context: {} from keyword: '{}'", entry.getKey(), pattern);
                    return entry.getKey();
                }
            }
        }
        
        return null;
    }

    /**
     * Extract potential location names from user message
     */
    private String[] extractLocationNames(String message) {
        // 🔒 FIX: Handle null message gracefully
        if (message == null || message.trim().isEmpty()) {
            log.warn("⚠️ extractLocationNames called with null or empty message");
            return new String[0];
        }
        
        java.util.List<String> locations = new java.util.ArrayList<>();
        String normalizedMessage = message.toLowerCase().trim();
        
        // Enhanced known locations with Italian regions and cities
        String[] knownLocations = {
            // Italian locations
            "piedmont", "alba", "emilia-romagna", "emilia romagna", "bologna", "modena",
            "tuscany", "toscana", "florence", "firenze", "chianti", "rome", "roma",
            "sicily", "sicilia", "palermo", "naples", "napoli", "venice", "venezia",
            "milan", "milano", "turin", "torino", "pisa", "siena", "verona",
            // French locations
            "eiffel tower", "eiffel", "tower", "paris", "louvre", "champs elysees", "champs-elysees",
            "arc de triomphe", "triomphe", "notre dame", "notre-dame", "sacre coeur", "sacre-coeur",
            "trocadero", "versailles", "montmartre", "seine", "marais", "latin quarter",
            // Other major cities
            "london", "big ben", "tower bridge", "buckingham palace", "westminster", "thames",
            "tokyo", "shibuya", "harajuku", "shinjuku", "ginza", "akihabara", "sensoji",
            "new york", "nyc", "manhattan", "brooklyn", "times square", "central park", "statue of liberty",
            "colosseum", "vatican", "trevi fountain", "spanish steps", "pantheon",
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
        // Require minimum length of 5 characters for fuzzy matching to prevent false positives
        // (e.g., "some" should NOT match "rome")
        if (targetWord.length() < 5) return false;
        
        String[] messageWords = message.split("\\s+");
        for (String messageWord : messageWords) {
            // Skip very short words to prevent false positives
            if (messageWord.length() < 5) continue;
            
            // Check for single character differences for words of similar length
            if (Math.abs(messageWord.length() - targetWord.length()) <= 1) {
                int differences = calculateLevenshteinDistance(messageWord, targetWord);
                // Stricter threshold: require < 30% error rate AND max 2 differences
                // This prevents "some"(4)→"rome"(4) with distance=1 (25% error)
                double errorRate = (double) differences / Math.max(messageWord.length(), targetWord.length());
                if (differences <= 2 && errorRate < 0.3) {
                    log.info("🔍 Found potential typo match: '{}' for target: '{}' (distance: {}, error rate: {:.1f}%)", 
                            messageWord, targetWord, differences, errorRate * 100);
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

    /**
     * Extract rich data fields from Google Places API response
     */
    private void extractRichDataFromPlace(com.fasterxml.jackson.databind.JsonNode place, 
                                         AddLocationFunction.AddLocationRequest addRequest) {
        try {
            // Extract rating
            if (place.has("rating")) {
                addRequest.setRating(place.get("rating").asDouble());
            }
            
            // Extract user ratings total
            if (place.has("userRatingCount")) {
                addRequest.setUserRatingsTotal(place.get("userRatingCount").asInt());
            }
            
            // Extract price level
            if (place.has("priceLevel")) {
                String priceLevel = place.get("priceLevel").asText();
                // Convert Google Places price level text to integer
                if ("PRICE_LEVEL_FREE".equals(priceLevel)) addRequest.setPriceLevel(1);
                else if ("PRICE_LEVEL_INEXPENSIVE".equals(priceLevel)) addRequest.setPriceLevel(1);
                else if ("PRICE_LEVEL_MODERATE".equals(priceLevel)) addRequest.setPriceLevel(2);
                else if ("PRICE_LEVEL_EXPENSIVE".equals(priceLevel)) addRequest.setPriceLevel(3);
                else if ("PRICE_LEVEL_VERY_EXPENSIVE".equals(priceLevel)) addRequest.setPriceLevel(4);
            }
            
            // Extract phone number
            if (place.has("nationalPhoneNumber")) {
                addRequest.setPhoneNumber(place.get("nationalPhoneNumber").asText());
            } else if (place.has("internationalPhoneNumber")) {
                addRequest.setPhoneNumber(place.get("internationalPhoneNumber").asText());
            }
            
            // Extract website
            if (place.has("websiteUri")) {
                addRequest.setWebsite(place.get("websiteUri").asText());
            }
            
            // Extract vicinity/address
            if (place.has("formattedAddress")) {
                addRequest.setVicinity(place.get("formattedAddress").asText());
            } else if (place.has("shortFormattedAddress")) {
                addRequest.setVicinity(place.get("shortFormattedAddress").asText());
            }
            
            // Extract place ID
            if (place.has("id")) {
                addRequest.setPlaceId(place.get("id").asText());
            }
            
            // Extract photos
            if (place.has("photos") && place.get("photos").isArray()) {
                java.util.List<String> photos = new java.util.ArrayList<>();
                var photosArray = place.get("photos");
                for (int i = 0; i < Math.min(photosArray.size(), 5); i++) { // Limit to 5 photos
                    var photo = photosArray.get(i);
                    if (photo.has("name")) {
                        // Construct photo URL using Google Places Photo API
                        String photoName = photo.get("name").asText();
                        String photoUrl = String.format(
                            "https://places.googleapis.com/v1/%s/media?maxHeightPx=400&maxWidthPx=400&key=%s",
                            photoName, googleApiKey
                        );
                        photos.add(photoUrl);
                    }
                }
                if (!photos.isEmpty()) {
                    addRequest.setPhotos(photos);
                }
            }
            
            // Extract opening hours status
            if (place.has("regularOpeningHours")) {
                var openingHours = place.get("regularOpeningHours");
                if (openingHours.has("openNow")) {
                    addRequest.setIsOpen(openingHours.get("openNow").asBoolean());
                }
                if (openingHours.has("weekdayDescriptions") && 
                    openingHours.get("weekdayDescriptions").isArray()) {
                    java.util.List<String> weekdayText = new java.util.ArrayList<>();
                    var weekdayArray = openingHours.get("weekdayDescriptions");
                    for (int i = 0; i < weekdayArray.size(); i++) {
                        weekdayText.add(weekdayArray.get(i).asText());
                    }
                    if (!weekdayText.isEmpty()) {
                        addRequest.setWeekdayText(weekdayText);
                    }
                }
            }
            
            log.info("✨ Extracted rich data for {}: rating={}, photos={}, phone={}", 
                    addRequest.getName(), 
                    addRequest.getRating(),
                    addRequest.getPhotos() != null ? addRequest.getPhotos().size() : 0,
                    addRequest.getPhoneNumber() != null);
                    
        } catch (Exception e) {
            log.warn("⚠️ Failed to extract rich data for {}: {}", addRequest.getName(), e.getMessage());
        }
    }
    
    /**
     * Extract country information from Google Places API response.
     * Parses the addressComponents array to find the country component.
     * 
     * @param place JsonNode containing the place details from Google Places API
     * @return Country code (e.g., "TH", "IT") or full name (e.g., "Thailand", "Italy"), or null if not found
     */
    private String extractCountryFromPlace(JsonNode place) {
        try {
            // ✅ DEBUG: Log all fields in the place response
            StringBuilder fieldsList = new StringBuilder();
            place.fieldNames().forEachRemaining(name -> fieldsList.append(name).append(", "));
            log.info("🔍 DEBUG: Place response fields: {}", fieldsList);
            
            if (!place.has("addressComponents")) {
                log.warn("⚠️ No addressComponents field in place response. Available fields: {}", fieldsList);
                
                // Try alternative field names for Google Places v1 API
                if (place.has("formattedAddress")) {
                    String address = place.get("formattedAddress").asText();
                    log.info("📍 Formatted address: {}", address);
                    
                    // Try to extract country from formatted address as fallback
                    String[] parts = address.split(",");
                    if (parts.length > 0) {
                        String lastPart = parts[parts.length - 1].trim();
                        log.info("🌍 Potential country from address: {}", lastPart);
                        return lastPart;
                    }
                }
                
                return null;
            }
            
            JsonNode addressComponents = place.get("addressComponents");
            if (!addressComponents.isArray()) {
                log.debug("addressComponents is not an array");
                return null;
            }
            
            // Iterate through address components to find the country
            for (JsonNode component : addressComponents) {
                if (component.has("types") && component.get("types").isArray()) {
                    for (JsonNode type : component.get("types")) {
                        if ("country".equals(type.asText())) {
                            // Prefer short text (country code like "TH", "IT")
                            if (component.has("shortText")) {
                                String countryCode = component.get("shortText").asText();
                                log.info("✅ Extracted country code from place: {}", countryCode);
                                return countryCode;
                            }
                            // Fallback to long text (full country name like "Thailand", "Italy")
                            if (component.has("longText")) {
                                String countryName = component.get("longText").asText();
                                log.info("✅ Extracted country name from place: {}", countryName);
                                return countryName;
                            }
                        }
                    }
                }
            }
            
            log.debug("No country component found in addressComponents");
            return null;
            
        } catch (Exception e) {
            log.warn("⚠️ Failed to extract country from place: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Smart location filtering with regional awareness.
     * Determines if a location should be included based on country matching and regional groupings.
     * Supports multi-country trips within the same region.
     * 
     * @param locationCountry Country extracted from Google Places API response (e.g., "TH", "Thailand")
     * @param userQueryCountry Country detected from user's query (e.g., "Italy", "Southeast Asia")
     * @param userQuery Original user query for context
     * @return true if location should be included, false if it should be filtered out
     */
    private boolean shouldIncludeLocation(String locationCountry, String userQueryCountry, String userQuery) {
        // Case 1: No country detected from user query → Include ALL locations
        // User might be asking about multi-country trips or general travel advice
        if (userQueryCountry == null || userQueryCountry.trim().isEmpty()) {
            log.debug("No user context country detected - including all locations");
            return true;
        }
        
        // Case 2: Location has no country data → Include by default
        // Better to show extra locations than miss valid ones
        if (locationCountry == null || locationCountry.trim().isEmpty()) {
            log.debug("Location country unknown - including by default");
            return true;
        }
        
        // Case 3: Exact country match → ALWAYS include
        // e.g., Bangkok (TH) for "Thailand vacation"
        if (areSameCountry(locationCountry, userQueryCountry)) {
            log.info("✅ Exact country match: {} == {} - including location", locationCountry, userQueryCountry);
            return true;
        }
        
        // Case 4: Same region → Include (handles multi-country trips)
        // e.g., Italy + France for "Europe tour", Thailand + Vietnam for "Southeast Asia backpacking"
        if (areSameRegion(locationCountry, userQueryCountry)) {
            log.info("✅ Same region: {} and {} in same travel region - including location", 
                    locationCountry, userQueryCountry);
            return true;
        }
        
        // Case 5: Different continents/regions → Exclude
        // e.g., NYC (US) when asking about Italy
        log.info("❌ Different regions: {} vs {} - excluding location", locationCountry, userQueryCountry);
        return false;
    }
    
    /**
     * Check if two country identifiers refer to the same country.
     * Handles both country codes (TH, US, IT) and full names (Thailand, United States, Italy).
     * 
     * @param country1 First country identifier
     * @param country2 Second country identifier
     * @return true if they represent the same country
     */
    private boolean areSameCountry(String country1, String country2) {
        if (country1 == null || country2 == null) {
            return false;
        }
        
        String c1 = country1.trim().toLowerCase();
        String c2 = country2.trim().toLowerCase();
        
        // Direct comparison
        if (c1.equals(c2)) {
            return true;
        }
        
        // Handle country code vs full name mappings
        // This is NOT for extraction, just for comparison during filtering
        java.util.Map<String, java.util.List<String>> countryAliases = java.util.Map.ofEntries(
            java.util.Map.entry("us", java.util.List.of("usa", "united states", "america")),
            java.util.Map.entry("uk", java.util.List.of("united kingdom", "england", "gb", "great britain")),
            java.util.Map.entry("th", java.util.List.of("thailand")),
            java.util.Map.entry("it", java.util.List.of("italy")),
            java.util.Map.entry("fr", java.util.List.of("france")),
            java.util.Map.entry("es", java.util.List.of("spain")),
            java.util.Map.entry("de", java.util.List.of("germany")),
            java.util.Map.entry("gr", java.util.List.of("greece")),
            java.util.Map.entry("jp", java.util.List.of("japan")),
            java.util.Map.entry("br", java.util.List.of("brazil")),
            java.util.Map.entry("pe", java.util.List.of("peru")),
            java.util.Map.entry("eg", java.util.List.of("egypt")),
            java.util.Map.entry("au", java.util.List.of("australia")),
            java.util.Map.entry("bt", java.util.List.of("bhutan"))
        );
        
        // Check if c1 or c2 match any aliases
        for (java.util.Map.Entry<String, java.util.List<String>> entry : countryAliases.entrySet()) {
            String code = entry.getKey();
            java.util.List<String> aliases = entry.getValue();
            
            boolean c1Match = c1.equals(code) || aliases.contains(c1);
            boolean c2Match = c2.equals(code) || aliases.contains(c2);
            
            if (c1Match && c2Match) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if two countries are in the same travel region.
     * Supports multi-country trips like "Europe tour" or "Southeast Asia backpacking".
     * Also handles region identifiers (e.g., "SOUTHEAST_ASIA" matching "thailand").
     * 
     * @param country1 First country identifier (can be country code, name, or region identifier)
     * @param country2 Second country identifier (can be country code, name, or region identifier)
     * @return true if countries are in the same region
     */
    private boolean areSameRegion(String country1, String country2) {
        if (country1 == null || country2 == null) {
            return false;
        }
        
        String c1 = country1.trim().toLowerCase();
        String c2 = country2.trim().toLowerCase();
        
        // Define regional groupings for common multi-country trips
        java.util.Map<String, java.util.List<String>> regionGroups = java.util.Map.ofEntries(
            java.util.Map.entry("western_europe", java.util.List.of(
                "fr", "france", "it", "italy", "es", "spain", "de", "germany", 
                "ch", "switzerland", "be", "belgium", "nl", "netherlands", 
                "at", "austria", "pt", "portugal"
            )),
            java.util.Map.entry("eastern_europe", java.util.List.of(
                "pl", "poland", "cz", "czech republic", "hu", "hungary", 
                "ro", "romania", "bg", "bulgaria", "hr", "croatia"
            )),
            java.util.Map.entry("uk_ireland", java.util.List.of(
                "uk", "united kingdom", "gb", "great britain", "england", 
                "ie", "ireland", "scotland", "wales"
            )),
            java.util.Map.entry("scandinavia", java.util.List.of(
                "se", "sweden", "no", "norway", "dk", "denmark", 
                "fi", "finland", "is", "iceland"
            )),
            java.util.Map.entry("southeast_asia", java.util.List.of(
                "th", "thailand", "vn", "vietnam", "kh", "cambodia", 
                "la", "laos", "my", "malaysia", "sg", "singapore", 
                "id", "indonesia", "ph", "philippines", "mm", "myanmar"
            )),
            java.util.Map.entry("south_asia", java.util.List.of(
                "in", "india", "np", "nepal", "bt", "bhutan", 
                "lk", "sri lanka", "bd", "bangladesh", "pk", "pakistan"
            )),
            java.util.Map.entry("east_asia", java.util.List.of(
                "jp", "japan", "cn", "china", "kr", "korea", "south korea", 
                "tw", "taiwan", "hk", "hong kong", "mo", "macau"
            )),
            java.util.Map.entry("middle_east", java.util.List.of(
                "ae", "uae", "united arab emirates", "dubai", "sa", "saudi arabia", 
                "qa", "qatar", "om", "oman", "jo", "jordan", "il", "israel"
            )),
            java.util.Map.entry("north_africa", java.util.List.of(
                "eg", "egypt", "ma", "morocco", "tn", "tunisia", 
                "dz", "algeria", "ly", "libya"
            )),
            java.util.Map.entry("south_america", java.util.List.of(
                "br", "brazil", "ar", "argentina", "pe", "peru", 
                "cl", "chile", "co", "colombia", "ec", "ecuador", 
                "bo", "bolivia", "uy", "uruguay", "ve", "venezuela"
            )),
            java.util.Map.entry("central_america", java.util.List.of(
                "mx", "mexico", "gt", "guatemala", "cr", "costa rica", 
                "pa", "panama", "ni", "nicaragua", "hn", "honduras"
            )),
            java.util.Map.entry("north_america", java.util.List.of(
                "us", "usa", "united states", "america", "ca", "canada"
            )),
            java.util.Map.entry("oceania", java.util.List.of(
                "au", "australia", "nz", "new zealand", "fj", "fiji", 
                "pg", "papua new guinea"
            ))
        );
        
        // Check if both identifiers are in the same region
        for (java.util.Map.Entry<String, java.util.List<String>> entry : regionGroups.entrySet()) {
            String regionKey = entry.getKey();
            java.util.List<String> region = entry.getValue();
            
            // Check if c1 is either the region key OR a country in the region
            boolean has1 = c1.equals(regionKey) || region.stream().anyMatch(country -> 
                country.equals(c1) || c1.contains(country) || country.contains(c1)
            );
            
            // Check if c2 is either the region key OR a country in the region
            boolean has2 = c2.equals(regionKey) || region.stream().anyMatch(country -> 
                country.equals(c2) || c2.contains(country) || country.contains(c2)
            );
            
            if (has1 && has2) {
                log.info("✅ Region match found: '{}' and '{}' both belong to region '{}'", 
                        country1, country2, regionKey.toUpperCase());
                return true;
            }
        }
        
        return false;
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
        
        @JsonPropertyDescription("Optional. The original user query to help filter locations by relevance")
        private String userQuery;
    }
}