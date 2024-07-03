package com.springboot.FlomadAIplanner.ai.functions;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Description("Provides weather forecast for a given location and date.")
public class WeatherForecastFunction implements AIFunction<WeatherForecastFunction.WeatherForecastRequest, WeatherForecastFunction.WeatherForecastResponse> {

    @Override
    public WeatherForecastResponse apply(WeatherForecastRequest request) {
        log.info("Fetching weather forecast for location: {} on date: {}", request.getLocation(), request.getDate());
        // Mock implementation
        return new WeatherForecastResponse("Sunny", 25);
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
