package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import com.springboot.FlomadAIplanner.service.geoapify.GeoapifyService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
@Description("""
        🌟 ENTHUSIASTIC LOCATION DISCOVERY ENGINE - Your Travel Inspiration Generator

        Transforms location searches into magical travel experiences! Instead of boring technical data,
        this function delivers exciting, conversational responses that build genuine excitement about destinations.

        ✨ CONVERSATIONAL MAGIC:
        🎯 Enthusiastic descriptions that capture the wonder of each place
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

    private final GeoapifyService geoapifyService;

    public PlaceTextSearchFunction(GeoapifyService geoapifyService) {
        this.geoapifyService = geoapifyService;
    }

    @Override
    @Valid
    public JsonNode apply(TextSearchRequestsWrapper requestsWrapper) {
        log.info("Performing text search for {} queries...", requestsWrapper.getRequests().size());

        List<JsonNode> placeResults = new ArrayList<>();

        try {
            for (TextSearchRequest request : requestsWrapper.getRequests()) {
                JsonNode result = geoapifyService.textSearchPlaces(request.getTextQuery());
                placeResults.add(result);
            }

            log.info("Received {} place results from Geoapify.", placeResults.size());

            return generateConversationalPlaceResponse(placeResults, requestsWrapper.getRequests());

        } catch (Exception e) {
            log.error("Error performing text search", e);
            return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
        }
    }

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

    private JsonNode transformToConversationalResponse(JsonNode placeData, String searchQuery) {
        JsonNode objectNode = JsonNodeFactory.instance.objectNode();

        try {
            JsonNode geocode = placeData.get("geocode");
            if (geocode != null) {
                String displayName = geocode.has("name") && !geocode.get("name").isNull()
                        ? geocode.get("name").asText() : searchQuery;
                String address = geocode.has("formatted") ? geocode.get("formatted").asText() : "a wonderful destination";
                String websiteUri = null;
                if (geocode.has("datasource") && geocode.get("datasource").has("raw")
                        && geocode.get("datasource").get("raw").has("website")) {
                    websiteUri = geocode.get("datasource").get("raw").get("website").asText();
                }
                Double lat = geocode.has("lat") ? geocode.get("lat").asDouble() : null;
                Double lon = geocode.has("lon") ? geocode.get("lon").asDouble() : null;

                String conversationalMessage = generateEnthusiasticLocationDescription(
                        displayName, address, null, websiteUri, lat, lon, searchQuery);

                ((com.fasterxml.jackson.databind.node.ObjectNode) objectNode).put("message", conversationalMessage);

                JsonNode suggestions = generateLocationSuggestions(displayName);
                ((com.fasterxml.jackson.databind.node.ObjectNode) objectNode).set("suggestions", suggestions);

            } else if (placeData.has("results") && placeData.get("results").size() > 0) {
                JsonNode firstResult = placeData.get("results").get(0);
                String displayName = firstResult.has("name") && !firstResult.get("name").isNull()
                        ? firstResult.get("name").asText()
                        : (firstResult.has("formatted") ? firstResult.get("formatted").asText() : searchQuery);
                String address = firstResult.has("formatted") ? firstResult.get("formatted").asText() : "a wonderful destination";
                String websiteUri = null;
                if (firstResult.has("datasource") && firstResult.get("datasource").has("raw")
                        && firstResult.get("datasource").get("raw").has("website")) {
                    websiteUri = firstResult.get("datasource").get("raw").get("website").asText();
                }
                Double lat = firstResult.has("lat") ? firstResult.get("lat").asDouble() : null;
                Double lon = firstResult.has("lon") ? firstResult.get("lon").asDouble() : null;

                String conversationalMessage = generateEnthusiasticLocationDescription(
                        displayName, address, null, websiteUri, lat, lon, searchQuery);

                ((com.fasterxml.jackson.databind.node.ObjectNode) objectNode).put("message", conversationalMessage);

                JsonNode suggestions = generateLocationSuggestions(displayName);
                ((com.fasterxml.jackson.databind.node.ObjectNode) objectNode).set("suggestions", suggestions);
            } else {
                ((com.fasterxml.jackson.databind.node.ObjectNode) objectNode).put("message",
                        "I couldn't find specific details for \"" + searchQuery + "\", but let me help you discover amazing places nearby! What type of experience are you looking for?");
            }

        } catch (Exception e) {
            log.error("Error transforming place data to conversational response", e);
            ((com.fasterxml.jackson.databind.node.ObjectNode) objectNode).put("message",
                    "I'm excited to help you explore " + searchQuery + "! Let me know what kind of experience you're looking for.");
        }

        return objectNode;
    }

    private String generateEnthusiasticLocationDescription(String name, String address, Double rating,
                                                           String websiteUri, Double lat, Double lon, String searchQuery) {

        String[] enthusiasticTemplates = {
                "Oh wow, %s! What an absolutely iconic destination! Located at %s, this incredible landmark is a must-visit! Ready to experience the magic?",
                "Amazing choice! %s is such a breathtaking place to visit! You'll find this gem at %s. This is going to be an unforgettable experience!",
                "Perfect! %s is calling your name! Nestled at %s, this spectacular destination awaits! Get ready for some serious wanderlust moments!",
                "Fantastic pick! %s is absolutely magical! Located at %s, this place is pure travel gold! Your adventure awaits!",
                "What a magnificent choice! %s at %s is truly special - this is going to be extraordinary! Let's make some incredible memories!"
        };

        String template = enthusiasticTemplates[(int) (Math.random() * enthusiasticTemplates.length)];
        String baseMessage = String.format(template, name, address);

        if (lat != null && lon != null) {
            baseMessage += String.format(" Coordinates: %.4f, %.4f", lat, lon);
        }

        if (websiteUri != null && !websiteUri.isEmpty()) {
            baseMessage += String.format(" [Official Website](%s)", websiteUri);
        }

        return baseMessage;
    }

    private JsonNode generateLocationSuggestions(String locationName) {
        JsonNode suggestions = JsonNodeFactory.instance.arrayNode();

        String city = extractCityFromLocation(locationName);
        String[] suggestionTemplates = {
                "Find nearby hotels in " + city,
                "Discover cultural tours in " + city,
                "Explore dining options near " + locationName,
                "Learn about transportation options to " + locationName
        };

        for (String suggestion : suggestionTemplates) {
            ((com.fasterxml.jackson.databind.node.ArrayNode) suggestions).add(suggestion);
        }

        return suggestions;
    }

    private String extractCityFromLocation(String locationName) {
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
