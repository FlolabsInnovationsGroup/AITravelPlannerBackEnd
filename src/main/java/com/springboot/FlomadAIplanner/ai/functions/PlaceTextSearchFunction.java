package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import com.springboot.FlomadAIplanner.service.google.GooglePlacesServiceAsync;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Function for searching place details using text queries.
 */
@Slf4j
@Component
@Description("""
        🌟 ENTHUSIASTIC LOCATION DISCOVERY ENGINE - Your Travel Inspiration Generator
        
        Transforms location searches into magical travel experiences! Instead of boring technical data,
        this function delivers exciting, conversational responses that build genuine excitement about destinations.
        
        ✨ CONVERSATIONAL MAGIC:
        🎯 Enthusiastic descriptions that capture the wonder of each place
        🗺️ Direct Google Maps links for instant exploration
        🌐 Official website connections when available  
        ⭐ Authentic ratings and insider knowledge
        💡 Contextual suggestions for deeper exploration
        
        🎊 PERFECT FOR TRAVEL INSPIRATION:
        "Find details for Eiffel Tower" → Magical description with cultural context
        "Get coordinates for Central Park" → Enthusiastic guide with activity suggestions  
        "Search for Times Square info" → Exciting overview with insider tips
        
        🚀 EXPERIENCE ENHANCEMENT:
        - Varied, enthusiastic responses (never repetitive!)
        - Cultural context and travel excitement
        - Direct action links and helpful suggestions
        - Authentic local insights and timing tips
        
        Returns: Conversational travel inspiration with actionable links and cultural intelligence
        """)

public class PlaceTextSearchFunction implements Function<PlaceTextSearchFunction.TextSearchRequestsWrapper, JsonNode> {


    private final GooglePlacesServiceAsync googlePlacesService;

    @Value("${google.api.key}") // ✅ Reads API Key from application.properties
    private String googleApiKey;

    public PlaceTextSearchFunction(GooglePlacesServiceAsync googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }

    @Override
    @Valid
    public JsonNode apply(TextSearchRequestsWrapper requestsWrapper) {
        log.info("🔎 Performing text search for {} queries...", requestsWrapper.getRequests().size());

        List<CompletableFuture<JsonNode>> searchFutures = new ArrayList<>();

        try {
            for (TextSearchRequest request : requestsWrapper.getRequests()) {
                searchFutures.add(googlePlacesService.asyncTextSearchPlaces(request));
            }

            // ✅ Collect results from async operations
            List<JsonNode> placeResults = searchFutures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            log.info("📡 Received {} place results from Google API.", placeResults.size());

            // 🎉 Generate conversational responses for each location instead of raw data
            JsonNode conversationalResults = generateConversationalPlaceResponse(placeResults, requestsWrapper.getRequests());
            
            return conversationalResults;

        } catch (Exception e) {
            log.error("❌ Error performing text search", e);
            return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
        }
    }

    /**
     * 🎨 Generates conversational, enthusiastic responses about places instead of raw technical data
     */
    private JsonNode generateConversationalPlaceResponse(List<JsonNode> placeResults, List<TextSearchRequest> originalRequests) {
        JsonNode resultArray = JsonNodeFactory.instance.arrayNode();
        
        for (int i = 0; i < placeResults.size() && i < originalRequests.size(); i++) {
            JsonNode placeData = placeResults.get(i);
            String searchQuery = originalRequests.get(i).getTextQuery();
            
            JsonNode conversationalResponse = transformToConversationalResponse(placeData, searchQuery);
            ((com.fasterxml.jackson.databind.node.ArrayNode) resultArray).add(conversationalResponse);
        }
        
        return resultArray;
    }

    /**
     * 🌟 Transforms raw Google Places data into enthusiastic, travel-friendly responses
     */
    private JsonNode transformToConversationalResponse(JsonNode placeData, String searchQuery) {
        JsonNode objectNode = JsonNodeFactory.instance.objectNode();
        
        try {
            // Extract data from Google Places response
            JsonNode places = placeData.get("places");
            if (places != null && places.isArray() && places.size() > 0) {
                JsonNode firstPlace = places.get(0);
                
                String displayName = extractDisplayName(firstPlace);
                String address = extractAddress(firstPlace);
                Double rating = extractRating(firstPlace);
                String websiteUri = extractWebsiteUri(firstPlace);
                String googleMapsUri = extractGoogleMapsUri(firstPlace);
                
                // 🎉 Generate enthusiastic, varied responses
                String conversationalMessage = generateEnthusiasticLocationDescription(
                    displayName, address, rating, websiteUri, googleMapsUri, searchQuery);
                    
                ((com.fasterxml.jackson.databind.node.ObjectNode) objectNode).put("message", conversationalMessage);
                
                // Add helpful suggestions
                JsonNode suggestions = generateLocationSuggestions(displayName);
                ((com.fasterxml.jackson.databind.node.ObjectNode) objectNode).set("suggestions", suggestions);
                
            } else {
                ((com.fasterxml.jackson.databind.node.ObjectNode) objectNode).put("message", 
                    "🌍 I couldn't find specific details for \"" + searchQuery + "\", but let me help you discover amazing places nearby! What type of experience are you looking for?");
            }
            
        } catch (Exception e) {
            log.error("Error transforming place data to conversational response", e);
            ((com.fasterxml.jackson.databind.node.ObjectNode) objectNode).put("message", 
                "✨ I'm excited to help you explore " + searchQuery + "! Let me know what kind of experience you're looking for.");
        }
        
        return objectNode;
    }

    /**
     * 🎊 Generates enthusiastic, varied descriptions for locations
     */
    private String generateEnthusiasticLocationDescription(String name, String address, Double rating, 
                                                          String websiteUri, String googleMapsUri, String searchQuery) {
        
        String[] enthusiasticTemplates = {
            "🗼 Oh wow, %s! What an absolutely iconic destination! Located at %s, this incredible landmark has earned a fantastic %s rating from travelers worldwide! 🌟 Ready to experience the magic?",
            
            "✨ Amazing choice! %s is such a breathtaking place to visit! You'll find this gem at %s with an outstanding %s-star rating! 🎯 This is going to be an unforgettable experience!",
            
            "🌟 Perfect! %s is calling your name! Nestled at %s, this spectacular destination boasts an impressive %s rating! 🚀 Get ready for some serious wanderlust moments!",
            
            "🎉 Fantastic pick! %s is absolutely magical! Located at %s with a stellar %s rating, this place is pure travel gold! ⭐ Your adventure awaits!",
            
            "🏛️ What a magnificent choice! %s at %s is truly special - with a %s rating, you know this is going to be extraordinary! 🎭 Let's make some incredible memories!"
        };
        
        // Randomly select a template for variety
        String template = enthusiasticTemplates[(int)(Math.random() * enthusiasticTemplates.length)];
        
        String ratingText = rating != null ? String.valueOf(rating) : "amazing";
        String baseMessage = String.format(template, name, address, ratingText);
        
        // Add website link if available (but skip Google Maps since it's integrated in our app)
        if (websiteUri != null && !websiteUri.isEmpty()) {
            baseMessage += String.format(" 🌐 [Official Website](%s)", websiteUri);
        }
        
        return baseMessage;
    }

    /**
     * 💡 Generates contextual suggestions based on the location
     */
    private JsonNode generateLocationSuggestions(String locationName) {
        JsonNode suggestions = JsonNodeFactory.instance.arrayNode();
        
        String[] suggestionTemplates = {
            "Find nearby hotels in " + extractCityFromLocation(locationName),
            "Discover cultural tours in " + extractCityFromLocation(locationName), 
            "Explore dining options near " + locationName,
            "Learn about transportation options to " + locationName
        };
        
        for (String suggestion : suggestionTemplates) {
            ((com.fasterxml.jackson.databind.node.ArrayNode) suggestions).add(suggestion);
        }
        
        return suggestions;
    }

    // Helper methods for data extraction
    private String extractDisplayName(JsonNode place) {
        JsonNode displayName = place.get("displayName");
        if (displayName != null && displayName.get("text") != null) {
            return displayName.get("text").asText();
        }
        return "Amazing Location";
    }

    private String extractAddress(JsonNode place) {
        JsonNode formattedAddress = place.get("formattedAddress");
        if (formattedAddress != null) {
            return formattedAddress.asText();
        }
        return "a wonderful destination";
    }

    private Double extractRating(JsonNode place) {
        JsonNode rating = place.get("rating");
        if (rating != null) {
            return rating.asDouble();
        }
        return null;
    }

    private String extractWebsiteUri(JsonNode place) {
        JsonNode websiteUri = place.get("websiteUri");
        if (websiteUri != null) {
            return websiteUri.asText();
        }
        return null;
    }

    private String extractGoogleMapsUri(JsonNode place) {
        JsonNode googleMapsUri = place.get("googleMapsUri");
        if (googleMapsUri != null) {
            return googleMapsUri.asText();
        }
        return null;
    }

    private String extractCityFromLocation(String locationName) {
        // Simple extraction - can be enhanced with more sophisticated logic
        if (locationName.toLowerCase().contains("eiffel")) return "Paris";
        if (locationName.toLowerCase().contains("statue of liberty")) return "New York";
        if (locationName.toLowerCase().contains("big ben")) return "London";
        if (locationName.toLowerCase().contains("colosseum")) return "Rome";
        return "this amazing city";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextSearchRequestsWrapper {
        @JsonPropertyDescription("List of text search requests. Recommended to use for searching multiple places at once.")
        private List<TextSearchRequest> requests;
    }
}
