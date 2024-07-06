package com.springboot.FlomadAIplanner.service.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.google.NearbyPlacesRequest;
import com.springboot.FlomadAIplanner.dto.google.TextSearchRequest;
import com.springboot.FlomadAIplanner.service.google.confg.GooglePlacesClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "googlePlacesClient", url = "${google.api.baseurl}", configuration = GooglePlacesClientConfig.class)
public interface GooglePlacesClient {

    @PostMapping(value = "/v1/places:searchNearby", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode searchNearbyPlaces(
            @RequestHeader("X-Goog-FieldMask") String fieldMask,
            @RequestBody NearbyPlacesRequest request);

    @PostMapping(value = "/v1/places:searchText", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode textSearchPlaces(
            @RequestHeader("X-Goog-FieldMask") String fieldMask,
            @RequestBody TextSearchRequest request);
}
