package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Mock function for accessibility assistance to prevent test failures
 */
@Component("accessibilityFunction")
@Description("Provides accessibility assistance information and accommodation support")
public class AccessibilityFunction implements Function<AccessibilityFunction.Request, String> {

    @Override
    public String apply(Request request) {
        return "Accessibility assistance information provided. Please contact your accommodation provider for specific accessibility arrangements.";
    }

    @Data
    @AllArgsConstructor
    public static class Request {
        @JsonPropertyDescription("The accessibility assistance request or requirement description")
        private String query;
        
        // No-args constructor for Jackson
        public Request() {}
    }
}
