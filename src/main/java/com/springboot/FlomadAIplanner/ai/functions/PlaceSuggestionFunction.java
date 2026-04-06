package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.service.geoapify.GeoapifyService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        
        IMPORTANT:
        ALWAYS INCLUDE required "includedTypes" in the request to filter the types of places to suggest.
        """)
public class PlaceSuggestionFunction implements Function<PlaceSuggestionFunction.NearbyPlacesRequestWrapper, List<JsonNode>> {

    private static final Map<String, String> TYPE_TO_CATEGORY = new HashMap<>();

    static {
        TYPE_TO_CATEGORY.put("restaurant", "catering.restaurant");
        TYPE_TO_CATEGORY.put("cafe", "catering.cafe");
        TYPE_TO_CATEGORY.put("bar", "catering.bar");
        TYPE_TO_CATEGORY.put("fast_food_restaurant", "catering.fast_food");
        TYPE_TO_CATEGORY.put("bakery", "catering.bakery");
        TYPE_TO_CATEGORY.put("coffee_shop", "catering.cafe");
        TYPE_TO_CATEGORY.put("ice_cream_shop", "catering.ice_cream");

        TYPE_TO_CATEGORY.put("hotel", "accommodation.hotel");
        TYPE_TO_CATEGORY.put("motel", "accommodation.motel");
        TYPE_TO_CATEGORY.put("hostel", "accommodation.hostel");
        TYPE_TO_CATEGORY.put("bed_and_breakfast", "accommodation.bed_and_breakfast");
        TYPE_TO_CATEGORY.put("campground", "accommodation.camping");
        TYPE_TO_CATEGORY.put("resort_hotel", "accommodation.hotel");
        TYPE_TO_CATEGORY.put("guest_house", "accommodation.guest_house");
        TYPE_TO_CATEGORY.put("lodging", "accommodation");

        TYPE_TO_CATEGORY.put("tourist_attraction", "tourism.attraction");
        TYPE_TO_CATEGORY.put("museum", "entertainment.museum");
        TYPE_TO_CATEGORY.put("art_gallery", "entertainment.museum");
        TYPE_TO_CATEGORY.put("amusement_park", "entertainment.theme_park");
        TYPE_TO_CATEGORY.put("zoo", "entertainment.zoo");
        TYPE_TO_CATEGORY.put("aquarium", "entertainment.aquarium");
        TYPE_TO_CATEGORY.put("park", "leisure.park");
        TYPE_TO_CATEGORY.put("national_park", "national_park");
        TYPE_TO_CATEGORY.put("historical_landmark", "tourism.attraction");
        TYPE_TO_CATEGORY.put("movie_theater", "entertainment.cinema");
        TYPE_TO_CATEGORY.put("night_club", "entertainment.nightclub");
        TYPE_TO_CATEGORY.put("casino", "entertainment.casino");
        TYPE_TO_CATEGORY.put("stadium", "sport.stadium");
        TYPE_TO_CATEGORY.put("bowling_alley", "entertainment.bowling_alley");
        TYPE_TO_CATEGORY.put("performing_arts_theater", "entertainment.theatre");

        TYPE_TO_CATEGORY.put("hospital", "healthcare.hospital");
        TYPE_TO_CATEGORY.put("pharmacy", "healthcare.pharmacy");
        TYPE_TO_CATEGORY.put("doctor", "healthcare.doctor_gp");
        TYPE_TO_CATEGORY.put("dentist", "healthcare.dentist");

        TYPE_TO_CATEGORY.put("shopping_mall", "commercial.shopping_mall");
        TYPE_TO_CATEGORY.put("supermarket", "commercial.supermarket");
        TYPE_TO_CATEGORY.put("grocery_store", "commercial.supermarket");
        TYPE_TO_CATEGORY.put("clothing_store", "commercial.clothing");
        TYPE_TO_CATEGORY.put("book_store", "commercial.books");
        TYPE_TO_CATEGORY.put("electronics_store", "commercial.electronics");
        TYPE_TO_CATEGORY.put("convenience_store", "commercial.convenience");

        TYPE_TO_CATEGORY.put("airport", "transport.airport");
        TYPE_TO_CATEGORY.put("train_station", "transport.train_station");
        TYPE_TO_CATEGORY.put("bus_station", "transport.bus_station");
        TYPE_TO_CATEGORY.put("subway_station", "transport.subway_entrance");
        TYPE_TO_CATEGORY.put("taxi_stand", "transport.taxi");
        TYPE_TO_CATEGORY.put("gas_station", "service.vehicle.fuel");
        TYPE_TO_CATEGORY.put("parking", "parking");
        TYPE_TO_CATEGORY.put("car_rental", "rental.car");

        TYPE_TO_CATEGORY.put("bank", "service.financial.bank");
        TYPE_TO_CATEGORY.put("atm", "service.financial.atm");
        TYPE_TO_CATEGORY.put("post_office", "service.post_office");
        TYPE_TO_CATEGORY.put("police", "service.police");
        TYPE_TO_CATEGORY.put("fire_station", "service.fire_station");
        TYPE_TO_CATEGORY.put("library", "education.library");
        TYPE_TO_CATEGORY.put("university", "education.university");
        TYPE_TO_CATEGORY.put("church", "religion.christian");
        TYPE_TO_CATEGORY.put("mosque", "religion.muslim");
        TYPE_TO_CATEGORY.put("synagogue", "religion.jewish");
        TYPE_TO_CATEGORY.put("hindu_temple", "religion.hindu");
        TYPE_TO_CATEGORY.put("spa", "leisure.spa");
        TYPE_TO_CATEGORY.put("gym", "sport.fitness");
        TYPE_TO_CATEGORY.put("fitness_center", "sport.fitness");
        TYPE_TO_CATEGORY.put("swimming_pool", "sport.swimming_pool");
        TYPE_TO_CATEGORY.put("golf_course", "sport.golf");

        TYPE_TO_CATEGORY.put("beauty_salon", "service.beauty");
        TYPE_TO_CATEGORY.put("hair_salon", "service.beauty.hairdresser");
        TYPE_TO_CATEGORY.put("laundry", "service.laundry");
        TYPE_TO_CATEGORY.put("veterinary_care", "service.pet");
        TYPE_TO_CATEGORY.put("travel_agency", "office.travel_agent");
    }

    private final GeoapifyService geoapifyService;

    public PlaceSuggestionFunction(GeoapifyService geoapifyService) {
        this.geoapifyService = geoapifyService;
    }

    @Valid
    public List<JsonNode> apply(NearbyPlacesRequestWrapper requestWrapper) {
        List<JsonNode> responses = new ArrayList<>();

        for (NearbyPlacesRequest request : requestWrapper.getRequests()) {
            double lat = request.getLocationRestriction().getCircle().getCenter().getLatitude();
            double lon = request.getLocationRestriction().getCircle().getCenter().getLongitude();
            double radius = request.getLocationRestriction().getCircle().getRadius();

            String categories = mapTypesToCategories(request.getIncludedTypes());

            log.info("Fetching place suggestions for location: {}|{} with categories: {}", lat, lon, categories);

            try {
                JsonNode response = geoapifyService.searchNearbyPlaces(lat, lon, radius, categories);
                log.info("Received place suggestions from Geoapify. Response length: {}", response.toString().length());
                responses.add(response);
            } catch (Exception e) {
                log.error("Error fetching place suggestions", e);
                responses.add(JsonNodeFactory.instance.objectNode().put("error", e.getMessage()));
            }
        }

        return responses;
    }

    private String mapTypesToCategories(String[] types) {
        if (types == null || types.length == 0) {
            return "";
        }

        return java.util.Arrays.stream(types)
                .map(type -> TYPE_TO_CATEGORY.getOrDefault(type, type))
                .distinct()
                .collect(Collectors.joining(","));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NearbyPlacesRequestWrapper {

        @Valid
        @JsonPropertyDescription("List of nearby places requests containing the search terms and filters for each search.")
        private List<NearbyPlacesRequest> requests;
    }
}
