package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@Description("Get Current Time.")
public class CurrentTimeFunction implements AIFunction<CurrentTimeFunction.CurrentTimeRequest, CurrentTimeFunction.CurrentTimeResponse>  {

    @Override
    public CurrentTimeResponse apply(CurrentTimeRequest request) {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = currentTime.format(formatter);
        log.info("Current Time is {}", formattedTime);
        return new CurrentTimeResponse(formattedTime);
    }

    @Data
    public static class CurrentTimeRequest {
        private String location;
    }

    @Data
    public static class CurrentTimeResponse {
        private final String currentTime;
    }
}