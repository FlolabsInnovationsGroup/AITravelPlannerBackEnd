package com.springboot.FlomadAIplanner.config;

import com.springboot.FlomadAIplanner.dto.positionstack.PositionStackResponse;
import com.springboot.FlomadAIplanner.service.positionstack.PositionStackClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

@TestConfiguration
public class TestPositionStackConfig {

    @Bean
    public PositionStackClient testPositionStackClient() {
        return new TestPositionStackClient();
    }

    private static class TestPositionStackClient implements PositionStackClient {
        @Override
        public PositionStackResponse forwardGeocode(String query, Integer limit, String country, String region, String output) {
            // Return mock data for testing
            PositionStackResponse response = new PositionStackResponse();
            PositionStackResponse.PositionStackData data = new PositionStackResponse.PositionStackData();
            data.setLatitude(40.7128);
            data.setLongitude(-74.0060);
            data.setName("Test Location");
            response.setData(List.of(data));
            return response;
        }
    }
}
