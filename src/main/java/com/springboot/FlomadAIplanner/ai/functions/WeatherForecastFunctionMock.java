package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
@Description("Provides weather forecasts for a location and date.")
public class WeatherForecastFunctionMock implements Function<WeatherForecastFunctionMock.WeatherForecastRequest, WeatherForecastFunctionMock.WeatherForecastResponse> {

    @Override
    public WeatherForecastResponse apply(WeatherForecastRequest request) {
        log.info("Fetching weather forecast for location: {} on date: {}", request.getLocation(), request.getDate());
        // Mock implementation
        return new WeatherForecastResponse("Sunny", 25, "Clear skies expected throughout the day");
    }

    @Data
    public static class WeatherForecastRequest {
        @JsonPropertyDescription("The location to get weather forecast for (e.g., 'Paris, France')")
        private String location;
        
        @JsonPropertyDescription("The date for the forecast in YYYY-MM-DD format")
        private String date;
    }

    @Data
    @AllArgsConstructor
    public static class WeatherForecastResponse {
        @JsonPropertyDescription("The weather condition (e.g., 'Sunny', 'Rainy', 'Cloudy')")
        private String weather;
        
        @JsonPropertyDescription("The temperature in Celsius")
        private int temperature;
        
        @JsonPropertyDescription("Additional weather details and recommendations")
        private String description;
    }
}
