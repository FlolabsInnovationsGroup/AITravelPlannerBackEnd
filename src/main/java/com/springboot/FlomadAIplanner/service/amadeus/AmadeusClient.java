package com.springboot.FlomadAIplanner.service.amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightOffersRequest;
import com.springboot.FlomadAIplanner.service.amadeus.confg.AmadeusClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "amadeusClient", url = "${amadeus.api.baseurl}", configuration = AmadeusClientConfig.class)
public interface AmadeusClient {

    /**
     * Implementation of the GET method to get flight destinations
     *
     * @param origin   - the origin location code
     * @param maxPrice - the maximum price
     */
    @GetMapping("/v1/shopping/flight-destinations")
    JsonNode getFlightDestinations(@RequestParam("origin") String origin,
                                   @RequestParam("maxPrice") String maxPrice);

    /**
     * Implementation of the GET method to get flight offers
     *
     * @param originLocationCode      - the origin location code
     * @param destinationLocationCode - the destination location code
     * @param departureDate           - the departure date
     * @param returnDate              - the return date
     * @param adults                  - the number of adults
     * @param max                     - the maximum number of offers to return
     */
    @GetMapping("/v2/shopping/flight-offers")
    JsonNode getFlightOffers(@RequestParam("originLocationCode") String originLocationCode,
                             @RequestParam("destinationLocationCode") String destinationLocationCode,
                             @RequestParam("departureDate") String departureDate,
                             @RequestParam("returnDate") String returnDate,
                             @RequestParam("adults") int adults,
                             @RequestParam("max") int max);

    /**
     * Implementation of the POST method to get flight offers
     * Not sure whether we are going to use it, but it's here as a good example
     *
     * @param httpMethodOverride  - the HTTP method override
     * @param flightOffersRequest - the flight offers request
     */
    @PostMapping(value = "/v1/shopping/flight-offers", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getFlightOffersPost(@RequestHeader("X-HTTP-Method-Override") String httpMethodOverride,
                                 @RequestBody FlightOffersRequest flightOffersRequest);

    /**
     * Implementation of the POST method to get flight offer pricing
     *
     * @param httpMethodOverride - the HTTP method override
     * @param offer              - the offer to get pricing for
     */
    @PostMapping(value = "/v1/shopping/flight-offers/pricing", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getOfferPricing(@RequestHeader("X-HTTP-Method-Override") String httpMethodOverride,
                             @RequestBody String offer);

    /**
     * Implementation of the GET method to get locations
     *
     * @param subType     - the sub type of the location
     * @param keyword     - the keyword to search for
     * @param countryCode - the country code to search in
     * @param sort        - the sort order
     * @param view        - the view to return
     */
    @GetMapping("/v1/reference-data/locations")
    JsonNode getLocations(@RequestParam("subType") String subType,
                          @RequestParam("keyword") String keyword,
                          @RequestParam(value = "countryCode", required = false) String countryCode,
                          @RequestParam(value = "sort", required = false) String sort,
                          @RequestParam(value = "view", required = false) String view);

    /**
     * Implementation of the GET method to get a location by ID
     *
     * @param locationId - the location ID
     */
    @GetMapping("/v1/reference-data/locations/{locationId}")
    JsonNode getLocationById(@PathVariable("locationId") String locationId);

}
