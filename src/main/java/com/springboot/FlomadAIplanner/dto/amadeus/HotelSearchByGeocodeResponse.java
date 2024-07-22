package com.springboot.FlomadAIplanner.dto.amadeus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.springboot.FlomadAIplanner.util.ToStringUtil;
import lombok.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelSearchByGeocodeResponse {

    @JsonPropertyDescription("List of hotel data.")
    private List<Hotel> data;

//    @JsonPropertyDescription("Meta information about the returned object(s) in 'data'")
//    private Meta meta;

    @JsonPropertyDescription("Error message if the search request fails.")
    private String errorMessage;

    @Override
    public String toString() {
        return ToStringUtil.toStringNonNullOnly(this);
    }

    public List<Hotel> getData() {
        return data == null ? List.of() : data.stream()
                .sorted(Comparator.comparing(Hotel::getLastUpdate).reversed())
                .limit(30)
                .collect(Collectors.toList());
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hotel {
        @JsonPropertyDescription("Location sub-type (e.g. airport, port, rail-station, restaurant, atm...)")
        private String subtype;

        @JsonPropertyDescription("Hotel Name")
        private String name;

        @JsonPropertyDescription("Olson format name (TZ) of the location time zone (https://en.wikipedia.org/wiki/List_of_tz_database_time_zones)")
        private String timeZoneName;

        @JsonPropertyDescription("IATA location code")
        private String iataCode;

//        @JsonPropertyDescription("Unique identifier of the hotel")
//        private long dupeId;

        @JsonPropertyDescription("Address information")
        private Address address;

//        @JsonPropertyDescription("Geographic coordinates describing the position of any location on the surface of Earth")
//        private GeoCode geoCode;

        @JsonPropertyDescription("Unique identifier of the hotel. Amadeus Property Code (8 chars)")
        private String hotelId;

//        @JsonPropertyDescription("Brand (RT...) or Merchant (AD...) (Amadeus 2 chars Code)")
//        private String chainCode;

        @JsonPropertyDescription("Distance information")
        private Distance distance;

        @JsonPropertyDescription("Last time the content was updated by a provider.")
        private String lastUpdate;

        @JsonPropertyDescription("This object refers to reviews with this star-rating.")
        private float rating;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        @JsonPropertyDescription("ISO 3166-1 country code")
        private String countryCode;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoCode {
        @JsonPropertyDescription("Latitude of the position expressed in decimal degrees")
        private double latitude;

        @JsonPropertyDescription("Longitude of the position expressed in decimal degrees")
        private double longitude;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Distance {
        @JsonPropertyDescription("Unit type")
        private String unit;

        @JsonPropertyDescription("Decimal distance value")
        private double value;

        @JsonPropertyDescription("Display value of the distance")
        private String displayValue;

//        @JsonPropertyDescription("Conveys whether the distance is limited or not")
//        private String isUnlimited;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        @JsonPropertyDescription("Total number of object(s) retrieved")
        private int count;

        @JsonPropertyDescription("Sorting fields criteria and their associated priority and direction")
        private List<String> sort;

        @JsonPropertyDescription("Links related to the returned object(s)")
        private Links links;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        @JsonPropertyDescription("Link to the same page")
        private String self;

        @JsonPropertyDescription("Link to the first page")
        private String first;

        @JsonPropertyDescription("Link to the previous page")
        private String prev;

        @JsonPropertyDescription("Link to the next page")
        private String next;

        @JsonPropertyDescription("Link to the last page")
        private String last;
    }
}
