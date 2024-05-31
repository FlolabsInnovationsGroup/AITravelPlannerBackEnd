package com.springboot.FlomadAIplanner.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.springboot.FlomadAIplanner.config.OpenTripMapConfig;

@Service
public class OpenTripMapService {
    private final RestTemplate restTemplate;
    private final OpenTripMapConfig openTripMapConfig;

    @Autowired
    public OpenTripMapService(RestTemplate restTemplate, OpenTripMapConfig openTripMapConfig) {
        this.restTemplate = restTemplate;
        this.openTripMapConfig = openTripMapConfig;
    }

    public String getPlaces(String lon, String lat, String radius) {
        String url = String.format("%s/radius?lon=%s&lat=%s&radius=%s&apikey=%s",
                openTripMapConfig.getBaseUrl(), lon, lat, radius, openTripMapConfig.getApiKey());
        return restTemplate.getForObject(url, String.class);
    }

    public String getPlaceDetails(String xid) {
        String url = String.format("%s/xid/%s?apikey=%s",
                openTripMapConfig.getBaseUrl(), xid, openTripMapConfig.getApiKey());
        return restTemplate.getForObject(url, String.class);
    }
}
