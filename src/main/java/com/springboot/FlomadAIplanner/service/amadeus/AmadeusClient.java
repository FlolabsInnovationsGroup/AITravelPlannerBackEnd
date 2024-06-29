package com.springboot.FlomadAIplanner.service.amadeus;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "amadeusClient", url = "https://test.api.amadeus.com")
public interface AmadeusClient {

    @GetMapping("/v1/shopping/flight-destinations")
    JsonNode getFlightDestinations(@RequestHeader("Authorization") String authorization,
                                   @RequestParam("origin") String origin,
                                   @RequestParam("maxPrice") String maxPrice);

    @GetMapping("/v2/shopping/flight-offers")
    JsonNode getFlightOffers(@RequestHeader("Authorization") String authorization,
                             @RequestParam("originLocationCode") String originLocationCode,
                             @RequestParam("destinationLocationCode") String destinationLocationCode,
                             @RequestParam("departureDate") String departureDate,
                             @RequestParam("returnDate") String returnDate,
                             @RequestParam("adults") int adults,
                             @RequestParam("max") int max);

    @PostMapping(value = "/v1/shopping/flight-offers/pricing", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode getOfferPricing(@RequestHeader("Authorization") String authorization,
                             @RequestHeader("X-HTTP-Method-Override") String httpMethodOverride,
                             @RequestBody String offer);

    @GetMapping("/v1/reference-data/locations")
    JsonNode getLocations(@RequestHeader("Authorization") String authorization,
                          @RequestParam("subType") String subType,
                          @RequestParam("keyword") String keyword,
                          @RequestParam(value = "countryCode", required = false) String countryCode,
                          @RequestParam(value = "sort", required = false) String sort,
                          @RequestParam(value = "view", required = false) String view);

    @GetMapping("/v1/reference-data/locations/{locationId}")
    JsonNode getLocationById(@RequestHeader("Authorization") String authorization,
                             @PathVariable("locationId") String locationId);

}
