package com.springboot.FlomadAIplanner.service.viator;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.FlomadAIplanner.dto.viator.AttractionProductsRequest;
import com.springboot.FlomadAIplanner.dto.viator.AttractionsRequest;
import com.springboot.FlomadAIplanner.dto.viator.FreeTextSearchRequest;
import com.springboot.FlomadAIplanner.service.viator.confg.ViatorClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "viatorClient", url = "${viator.api.baseurl}", configuration = ViatorClientConfig.class)
public interface ViatorClient {

    /**
     * Implementation of the GET method to get destinations
     *
     * @description <a href="https://docs.viator.com/partner-api/technical/#tag/V1-endpoints/operation/v1TaxonomyDestinations"/>
     */
    @GetMapping(value = "/v1/taxonomy/destinations", consumes = MediaType.APPLICATION_JSON_VALUE)
    String getDestinations();

    /**
     * Implementation of the POST method to get attractions
     *
     * @description <a href="https://docs.viator.com/partner-api/technical/#tag/V1-endpoints/operation/v1TaxonomyAttractions"/>
     */
    @PostMapping(value = "/v1/taxonomy/attractions", consumes = MediaType.APPLICATION_JSON_VALUE)
    String getAttractions(@RequestHeader("Accept-Language") String acceptLanguage, @RequestBody AttractionsRequest attractionsRequest);

    /**
     * Implementation of the POST method to get attraction products
     *
     * @description <a href="https://docs.viator.com/partner-api/technical/#tag/V1-endpoints-(affiliates-only)/operation/v1AttractionProducts"/>
     */
    @PostMapping(value = "/v1/attraction/products", consumes = MediaType.APPLICATION_JSON_VALUE)
    String getAttractionProducts(@RequestHeader("Accept-Language") String acceptLanguage, @RequestBody AttractionProductsRequest attractionProductsRequest);

    /**
     * Implementation of the POST method to search free text
     *
     * @description <a href="https://docs.viator.com/partner-api/technical/#tag/Auxiliary/operation/searchFreeText"/>
     */
    @PostMapping(value = "/search/freetext", consumes = MediaType.APPLICATION_JSON_VALUE)
    JsonNode searchFreeText(@RequestHeader("Accept") String accept, @RequestHeader("Accept-Language") String acceptLanguage, @RequestBody FreeTextSearchRequest freeTextSearchRequest);

}
