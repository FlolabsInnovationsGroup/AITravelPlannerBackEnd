package com.springboot.FlomadAIplanner.dto.google;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Nearby Places Request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyPlacesRequest {

    @NotNull
    @JsonPropertyDescription("REQUIRED. Defines the region to search specified as a circle, with a center point and radius in meters.")
    private LocationRestriction locationRestriction;

    @Positive
    @JsonPropertyDescription("REQUIRED. Specifies the maximum number of results to return. Must be a positive integer.")
    private int maxResultCount;

    @NotNull
    @JsonPropertyDescription("""
            REQUIRED. Specifies the types of places to include in the search results. Must be a non-null array of place types. Supported types include various categories such as:\s
            Automotive: 'car_dealer', 'car_rental', 'car_repair', 'car_wash', 'electric_vehicle_charging_station', 'gas_station', 'parking', 'rest_stop'.
            Business: 'farm'.
            Culture: 'art_gallery', 'museum', 'performing_arts_theater'.
            Education: 'library', 'preschool', 'primary_school', 'secondary_school', 'university'.
            Entertainment and Recreation: 'amusement_center', 'amusement_park', 'aquarium', 'banquet_hall', 'bowling_alley', 'casino', 'community_center', 'convention_center', 'cultural_center', 'dog_park', 'event_venue', 'hiking_area', 'historical_landmark', 'marina', 'movie_rental', 'movie_theater', 'national_park', 'night_club', 'park', 'tourist_attraction', 'visitor_center', 'wedding_venue', 'zoo'.
            Finance: 'accounting', 'atm', 'bank'.
            Food and Drink: 'american_restaurant', 'bakery', 'bar', 'barbecue_restaurant', 'brazilian_restaurant', 'breakfast_restaurant', 'brunch_restaurant', 'cafe', 'chinese_restaurant', 'coffee_shop', 'fast_food_restaurant', 'french_restaurant', 'greek_restaurant', 'hamburger_restaurant', 'ice_cream_shop', 'indian_restaurant', 'indonesian_restaurant', 'italian_restaurant', 'japanese_restaurant', 'korean_restaurant', 'lebanese_restaurant', 'meal_delivery', 'meal_takeaway', 'mediterranean_restaurant', 'mexican_restaurant', 'middle_eastern_restaurant', 'pizza_restaurant', 'ramen_restaurant', 'restaurant', 'sandwich_shop', 'seafood_restaurant', 'spanish_restaurant', 'steak_house', 'sushi_restaurant', 'thai_restaurant', 'turkish_restaurant', 'vegan_restaurant', 'vegetarian_restaurant', 'vietnamese_restaurant'.
            Geographical Areas: 'administrative_area_level_1', 'administrative_area_level_2', 'country', 'locality', 'postal_code', 'school_district'.
            Government: 'city_hall', 'courthouse', 'embassy', 'fire_station', 'local_government_office', 'police', 'post_office'.
            Health and Wellness: 'dental_clinic', 'dentist', 'doctor', 'drugstore', 'hospital', 'medical_lab', 'pharmacy', 'physiotherapist', 'spa'.
            Lodging: 'bed_and_breakfast', 'campground', 'camping_cabin', 'cottage', 'extended_stay_hotel', 'farmstay', 'guest_house', 'hostel', 'hotel', 'lodging', 'motel', 'private_guest_room', 'resort_hotel', 'rv_park'.
            Places of Worship: 'church', 'hindu_temple', 'mosque', 'synagogue'.
            Services: 'barber_shop', 'beauty_salon', 'cemetery', 'child_care_agency', 'consultant', 'courier_service', 'electrician', 'florist', 'funeral_home', 'hair_care', 'hair_salon', 'insurance_agency', 'laundry', 'lawyer', 'locksmith', 'moving_company', 'painter', 'plumber', 'real_estate_agency', 'roofing_contractor', 'storage', 'tailor', 'telecommunications_service_provider', 'travel_agency', 'veterinary_care'.
            Shopping: 'auto_parts_store', 'bicycle_store', 'book_store', 'cell_phone_store', 'clothing_store', 'convenience_store', 'department_store', 'discount_store', 'electronics_store', 'furniture_store', 'gift_shop', 'grocery_store', 'hardware_store', 'home_goods_store', 'home_improvement_store', 'jewelry_store', 'liquor_store', 'market', 'pet_store', 'shoe_store', 'shopping_mall', 'sporting_goods_store', 'store', 'supermarket', 'wholesaler'.
            Sports: 'athletic_field', 'fitness_center', 'golf_course', 'gym', 'playground', 'ski_resort', 'sports_club', 'sports_complex', 'stadium', 'swimming_pool'.
            Transportation: 'airport', 'bus_station', 'bus_stop', 'ferry_terminal', 'heliport', 'light_rail_station', 'park_and_ride', 'subway_station', 'taxi_stand', 'train_station', 'transit_depot', 'transit_station', 'truck_stop'.
            Additional types for Place Autocomplete: 'administrative_area_level_3', 'administrative_area_level_4', 'administrative_area_level_5', 'administrative_area_level_6', 'administrative_area_level_7', 'archipelago', 'colloquial_area', 'continent', 'establishment', 'floor', 'food', 'general_contractor', 'geocode', 'health', 'intersection', 'landmark', 'natural_feature', 'neighborhood', 'place_of_worship', 'plus_code', 'point_of_interest', 'political', 'post_box', 'postal_code_prefix', 'postal_code_suffix', 'postal_town', 'premise', 'room', 'route', 'street_address', 'street_number', 'sublocality', 'sublocality_level_1', 'sublocality_level_2', 'sublocality_level_3', 'sublocality_level_4', 'sublocality_level_5', 'subpremise', 'town_square'.""")
    private String[] includedTypes;

    /**
     * Nested class for defining the location restriction as a circle.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationRestriction {
        @NotNull
        @JsonPropertyDescription("REQUIRED. Defines the search area as a circle.")
        private Circle circle;

        /**
         * Nested class for defining the circle.
         * Th Circle defines a circular area for a nearby place request.
         * It allows you to specify the center coordinates (latitude and longitude)
         * and radius to restrict search results to locations within that geographic range.
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Circle {
            @NotNull
            @JsonPropertyDescription("REQUIRED. Defines the center of the search area circle.")
            private Center center;

            @Positive
            @JsonPropertyDescription("REQUIRED. Radius must be in [0, 50000] inclusively. Radius of the search area circle in meters.")
            private double radius;

                         
             /**
             * Nested class for defining the center of the circle.
             */
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Center {
                @Positive
                @JsonPropertyDescription("REQUIRED. Latitude of the search area center. Must be a positive number.")
                private double latitude;

                @Positive
                @JsonPropertyDescription("REQUIRED. Longitude of the search area center. Must be a positive number.")
                private double longitude;
            }
        }
    }
}
