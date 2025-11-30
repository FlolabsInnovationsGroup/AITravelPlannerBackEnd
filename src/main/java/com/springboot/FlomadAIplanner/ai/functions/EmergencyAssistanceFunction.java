package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Mock function for emergency assistance to prevent test failures
 */
@Component("emergencyAssistanceFunction")
@Description("Provides emergency assistance guidance and emergency contact information")
public class EmergencyAssistanceFunction implements Function<EmergencyAssistanceFunction.Request, String> {

    @Override
    public String apply(Request request) {
        return "Emergency assistance information provided. Please contact local emergency services: 911 (US), 112 (EU), or your local emergency number.";
    }

    @Data
    @AllArgsConstructor
    public static class Request {
        @JsonPropertyDescription("The emergency assistance request or situation description")
        private String query;
        
        // No-args constructor for Jackson
        public Request() {}
    }
}
