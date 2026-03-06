package com.springboot.FlomadAIplanner.service.amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightOffersPostRequest;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightOffersPostResponse;
import com.springboot.FlomadAIplanner.dto.amadeus.HotelSearchByGeocodeResponse;
import com.springboot.FlomadAIplanner.service.amadeus.confg.AmadeusClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "amadeusClient", url = "${amadeus.api.baseurl}",
        configuration = AmadeusClientConfig.class)
public interface AmadeusClient {

    /**
     * GET flight destinations.
     *
     * @param origin origin code
     * @param maxPrice max price
     * @param departureDate optional departure date
     * @param oneWay optional one-way flag
     * @param duration optional duration
     * @param nonStop optional non-stop flag
     * @param viewBy optional view
     * @return JSON response
     */
    @GetMapping("/v1/shopping/flight-destinations")
    JsonNode getFlightDestinations(
            @RequestParam("origin") String origin,
            @RequestParam("maxPrice") int maxPrice,
            @RequestParam(value = "departureDate", required = false)
            String departureDate,
            @RequestParam(value = "oneWay", required = false) Boolean oneWay,
            @RequestParam(value = "duration", required = false) String duration,
            @RequestParam(value = "nonStop", required = false) Boolean nonStop,
            @RequestParam(value = "viewBy", required = false) String viewBy);

    /**
     * POST flight offers search.
     *
     * @param httpMethodOverride X-HTTP-Method-Override header
     * @param flightOffersRequest request body
     * @return flight offers response
     * @description <a href="https://developers.amadeus.com/self-service/category/flights/api-doc/flight-offers-search/api-reference">...</a>
     */
    @PostMapping(value = "/v2/shopping/flight-offers",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    FlightOffersPostResponse getFlightOffersPost(
            @RequestHeader("X-HTTP-Method-Override") String httpMethodOverride,
            @RequestBody FlightOffersPostRequest flightOffersRequest);

    /**
     * POST flight offer pricing.
     *
     * @param httpMethodOverride X-HTTP-Method-Override header
     * @param offer offer body
     * @return JSON response
     * @description <a href="https://developers.amadeus.com/self-service/category/flights/api-doc/flight-offers-price/api-reference">...</a>
     */
    @PostMapping(value = "/v1/shopping/flight-offers/pricing",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getOfferPricing(
            @RequestHeader("X-HTTP-Method-Override") String httpMethodOverride,
            @RequestBody String offer);

    /**
     * GET locations (airport/city search).
     *
     * @param subType subType
     * @param keyword keyword
     * @param countryCode optional country
     * @param pageLimit optional limit
     * @param pageOffset optional offset
     * @param sort optional sort
     * @param view optional view
     * @return JSON response
     * @description <a href="https://developers.amadeus.com/self-service/category/flights/api-doc/airport-and-city-search/api-reference">...</a>
     */
    @GetMapping("/v1/reference-data/locations")
    JsonNode getLocations(
            @RequestParam("subType") String subType,
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "countryCode", required = false)
            String countryCode,
            @RequestParam(value = "page[limit]", required = false)
            Integer pageLimit,
            @RequestParam(value = "page[offset]", required = false)
            Integer pageOffset,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "view", required = false) String view);

    /**
     * GET location by ID.
     *
     * @param locationId location id
     * @return JSON response
     * @description <a href="https://developers.amadeus.com/self-service/category/flights/api-doc/airport-and-city-search/api-reference">...</a>
     */
    @GetMapping("/v1/reference-data/locations/{locationId}")
    JsonNode getLocationById(
            @PathVariable("locationId") String locationId);

    /**
     * GET hotels by geocode.
     *
     * @param latitude latitude
     * @param longitude longitude
     * @param radius optional radius
     * @param radiusUnit optional unit
     * @param chainCodes optional chain codes
     * @param amenities optional amenities
     * @param ratings optional ratings
     * @param hotelSource optional source
     * @return hotel search response
     * @description <a href="https://developers.amadeus.com/self-service/category/hotels/api-doc/hotel-list/api-reference">...</a>
     */
    @GetMapping("/v1/reference-data/locations/hotels/by-geocode")
    HotelSearchByGeocodeResponse searchHotelsByGeocode(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(value = "radius", required = false) Integer radius,
            @RequestParam(value = "radiusUnit", required = false)
            String radiusUnit,
            @RequestParam(value = "chainCodes", required = false)
            List<String> chainCodes,
            @RequestParam(value = "amenities", required = false)
            List<String> amenities,
            @RequestParam(value = "ratings", required = false)
            List<Integer> ratings,
            @RequestParam(value = "hotelSource", required = false)
            String hotelSource);

    /**
     * GET multi hotel offers.
     *
     * @param hotelIds hotel IDs
     * @param adults adults
     * @param checkInDate check-in
     * @param checkOutDate check-out
     * @param countryOfResidence country
     * @param roomQuantity rooms
     * @param priceRange price range
     * @param currency currency
     * @param paymentPolicy payment policy
     * @param boardType board type
     * @param includeClosed include closed
     * @param bestRateOnly best rate only
     * @param lang language
     * @return JSON response
     * @description <a href="https://developers.amadeus.com/self-service/category/hotels/api-doc/hotel-search/api-reference">...</a>
     */
    @GetMapping("/v3/shopping/hotel-offers")
    JsonNode getMultiHotelOffers(
            @RequestParam("hotelIds") String hotelIds,
            @RequestParam(value = "adults", required = false, defaultValue = "1")
            Integer adults,
            @RequestParam(value = "checkInDate", required = false)
            String checkInDate,
            @RequestParam(value = "checkOutDate", required = false)
            String checkOutDate,
            @RequestParam(value = "countryOfResidence", required = false)
            String countryOfResidence,
            @RequestParam(value = "roomQuantity", required = false,
                    defaultValue = "1") Integer roomQuantity,
            @RequestParam(value = "priceRange", required = false)
            String priceRange,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "paymentPolicy", required = false,
                    defaultValue = "NONE") String paymentPolicy,
            @RequestParam(value = "boardType", required = false)
            String boardType,
            @RequestParam(value = "includeClosed", required = false)
            Boolean includeClosed,
            @RequestParam(value = "bestRateOnly", required = false,
                    defaultValue = "true") Boolean bestRateOnly,
            @RequestParam(value = "lang", required = false) String lang);
}
