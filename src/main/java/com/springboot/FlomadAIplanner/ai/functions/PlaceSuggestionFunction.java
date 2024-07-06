package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.exceptions.GoogleAPIException;
import com.springboot.FlomadAIplanner.service.google.GooglePlacesService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Component
@Description("""
        Provides place suggestions near the place we need
        based on location and types of the places to suggest.
        Use this function to get a list of places near a location that user is looking for.
        This function is useful for providing place suggestions to the user based on their location and preferences.
        Should be called after the CurrentLocationFunction to get the user's current location.
        P.S. If you have the accurate location details from the chat history,
        you can directly call this function to get place suggestions.
                
        The googleMapsAllPlacesDisplayLink field MUST NOT be changed in any way.
        MUST be returned to the user in the exact same view, as it is exists in this field.""")
//@Description("When user message contains word 'PLACES' call this function only.")
public class PlaceSuggestionFunction implements AIFunction<PlaceSuggestionFunction.NearbyPlacesRequestWrapper, PlaceSuggestionFunction.NearbyPlacesResponseWrapper> {

    @Value("${google.maps.places.template.fullurl}")
    private String baseUrl;

    private final GooglePlacesService googlePlacesService;

    @Autowired
    public PlaceSuggestionFunction(GooglePlacesService googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }


    @Valid
    @Override
    public NearbyPlacesResponseWrapper apply(NearbyPlacesRequestWrapper requestWrapper) {
        NearbyPlacesRequest request = requestWrapper.getRequest();
        log.info("Fetching place suggestions for location: {}|{} with types: {}", request.getLocationRestriction().getCircle().getCenter().getLatitude(), request.getLocationRestriction().getCircle().getCenter().getLongitude(), String.join(", ", request.getIncludedTypes()));

        try {
            NearbyPlacesResponseWrapper response = new NearbyPlacesResponseWrapper();

            JsonNode nearbyPlaces = googlePlacesService.searchNearbyPlaces(request);
            response.setResponseBody(nearbyPlaces);

            // Extract formatted addresses from the response
            List<String> places = extractFormattedAddresses(nearbyPlaces);
            // Generate Google Maps full link with destination and waypoints
            // example: https://www.google.com/maps/search/?api=1&query=Times+Square|Grand+Central+Terminal,89+E+42nd+St,New+York,NY+10017|Rockefeller+Center,45+Rockefeller+Plaza,New+York,NY+10111|Empire+State+Building,20+W+34th+St,New+York,NY+10001|American+Museum+of+Natural+History,200+Central+Park+W,New+York,NY+10024|Flatiron+Building,175+5th+Ave,New+York,NY+10010
            String mapsLink = generateMapsLink(requestWrapper.getDestination(), places);
            response.setGoogleMapsAllPlacesDisplayLink(mapsLink);
            log.info("Generated Google Maps link: {}", mapsLink);

            ((ObjectNode) nearbyPlaces).put("googleMapsAllPlacesDisplayLink", mapsLink);

            return response;
        } catch (GoogleAPIException e) {
            log.error("Error fetching place suggestions", e);
            return new NearbyPlacesResponseWrapper(JsonNodeFactory.instance.objectNode().put("error", e.getMessage()));
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NearbyPlacesRequestWrapper {

        @NotBlank
        @JsonPropertyDescription("Required. The destination place name. It is the same place name as we are looking for suggested places right now.")
        private String destination;

        @Valid
        @JsonPropertyDescription("Required. The nearby places request containing the search term and filters for the search.")
        private NearbyPlacesRequest request;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NearbyPlacesResponseWrapper {

        @NotBlank
        @JsonPropertyDescription("""
                MUST NOT be changed in any way. MUST be returned to the user in the exact same view,
                as it is exists in this field.
                The google maps link to display all the places on the map.
                Add this to the user response message everytime you call this function.
                Because this link contains really useful information for the user.""")
        private String googleMapsAllPlacesDisplayLink;

        @Valid
        @JsonPropertyDescription("The response from the Google Places API containing the nearby places list.")
        private JsonNode responseBody;

        public NearbyPlacesResponseWrapper(JsonNode error) {
            this.responseBody = error;
        }
    }

    private List<String> extractFormattedAddresses(JsonNode nearbyPlaces) {
        return StreamSupport.stream(nearbyPlaces.get("places").spliterator(), false)
                .map(place -> place.get("formattedAddress").asText())
                .collect(Collectors.toList());
    }

    private String generateMapsLink(String destination, List<String> places) {
        String waypoints = places.stream()
                        .map(this::encodeURIComponent)
                        .collect(Collectors.joining("|"));
       
        String query = "origin=" + encodeURIComponent(destination) +  "&destination=" + encodeURIComponent(destination) + "&waypoints=" + waypoints;


        return baseUrl + "?" + query;
    }

    private String encodeURIComponent(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }


}
