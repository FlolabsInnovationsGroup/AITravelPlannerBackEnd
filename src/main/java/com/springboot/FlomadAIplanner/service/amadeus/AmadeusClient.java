package com.springboot.FlomadAIplanner.service.amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.amadeus.FlightOffersPostRequest;
import com.springboot.FlomadAIplanner.service.amadeus.confg.AmadeusClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "amadeusClient", url = "${amadeus.api.baseurl}", configuration = AmadeusClientConfig.class)
public interface AmadeusClient {

    @GetMapping("/v1/shopping/flight-destinations")
    JsonNode getFlightDestinations(@RequestParam("origin") String origin,
                                   @RequestParam("maxPrice") int maxPrice,
                                   @RequestParam(value = "departureDate", required = false) String departureDate,
                                   @RequestParam(value = "oneWay", required = false) Boolean oneWay,
                                   @RequestParam(value = "duration", required = false) String duration,
                                   @RequestParam(value = "nonStop", required = false) Boolean nonStop,
                                   @RequestParam(value = "viewBy", required = false) String viewBy);

    @PostMapping(value = "/v1/shopping/flight-offers", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getFlightOffersPost(@RequestHeader("X-HTTP-Method-Override") String httpMethodOverride,
                                 @RequestBody FlightOffersPostRequest flightOffersRequest);

    @PostMapping(value = "/v1/shopping/flight-offers/pricing", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getOfferPricing(@RequestHeader("X-HTTP-Method-Override") String httpMethodOverride,
                             @RequestBody String offer);

    @GetMapping("/v1/reference-data/locations")
    JsonNode getLocations(@RequestParam("subType") String subType,
                          @RequestParam("keyword") String keyword,
                          @RequestParam(value = "countryCode", required = false) String countryCode,
                          @RequestParam(value = "page[limit]", required = false) Integer pageLimit,
                          @RequestParam(value = "page[offset]", required = false) Integer pageOffset,
                          @RequestParam(value = "sort", required = false) String sort,
                          @RequestParam(value = "view", required = false) String view);

    @GetMapping("/v1/reference-data/locations/{locationId}")
    JsonNode getLocationById(@PathVariable("locationId") String locationId);
}
