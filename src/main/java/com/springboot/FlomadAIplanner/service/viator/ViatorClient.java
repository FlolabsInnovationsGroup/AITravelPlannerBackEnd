package com.springboot.FlomadAIplanner.service.viator;

import com.springboot.FlomadAIplanner.dto.viator.AttractionProductsRequest;
import com.springboot.FlomadAIplanner.dto.viator.AttractionsRequest;
import com.springboot.FlomadAIplanner.service.viator.confg.ViatorClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "viatorClient", url = "${viator.api.baseurl}", configuration = ViatorClientConfig.class)
public interface ViatorClient {

    @GetMapping(value = "/v1/taxonomy/destinations", consumes = MediaType.APPLICATION_JSON_VALUE)
    String getDestinations();

    @PostMapping(value = "/v1/taxonomy/attractions", consumes = MediaType.APPLICATION_JSON_VALUE)
    String getAttractions(@RequestHeader("Accept-Language") String acceptLanguage, @RequestBody AttractionsRequest attractionsRequest);

    @PostMapping(value = "/v1/attraction/products", consumes = MediaType.APPLICATION_JSON_VALUE)
    String getAttractionProducts(@RequestHeader("Accept-Language") String acceptLanguage, @RequestBody AttractionProductsRequest attractionProductsRequest);
}
