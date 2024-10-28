package com.springboot.FlomadAIplanner.ai.functions;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Description("Provides weather forecasts for a list of locations and dates.")
public class WeatherForecastFunctionMock implements AIFunction<List<WeatherForecastFunctionMock.WeatherForecastRequest>, List<WeatherForecastFunctionMock.WeatherForecastResponse>> {

    @Override
    public List<WeatherForecastResponse> apply(List<WeatherForecastRequest> requests) {
        List<WeatherForecastResponse> responses = new ArrayList<>();
        for (WeatherForecastRequest request : requests) {
            log.info("Fetching weather forecast for location: {} on date: {}", request.getLocation(), request.getDate());
            // Mock implementation for each request
            WeatherForecastResponse response = new WeatherForecastResponse("Sunny", 25);
            responses.add(response);
        }
        return responses;
    }

    @Data
    public static class WeatherForecastRequest {
        private String location;
        private String date;
    }

    @Data
    public static class WeatherForecastResponse {
        private final String weather;
        private final int temperature;
    }
}
