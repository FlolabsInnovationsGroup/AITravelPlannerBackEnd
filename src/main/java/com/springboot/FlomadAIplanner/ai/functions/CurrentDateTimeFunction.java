package com.springboot.FlomadAIplanner.ai.functions;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

@Slf4j
@Component
@Description("""
        Retrieves current date and time in real-world context for travel planning. Essential for time-sensitive 
        operations like flight searches, tour availability, and booking deadlines.
        
        CRITICAL FIRST STEP: Must be called BEFORE any time-dependent functions to establish current context

        Features:
        - Real-time date/time retrieval
        - Customizable format specification
        - UTC and local time zone support
        - Essential for AI temporal awareness

        DEPENDENCY PATTERN: CurrentDateTimeFunction → Other time-sensitive functions

        Required for functions needing temporal context:
        - Flight searches (departure/arrival times)
        - Tour availability (seasonal offerings)  
        - Hotel booking (check-in/out dates)
        - Currency rates (current exchange rates)
        
        Examples: "What's today's date?", "Current time for booking", "Today's date for flight search"
        
        WORKFLOW: Always call this function first when users mention dates, times, or time-sensitive searches.
        """)
public class CurrentDateTimeFunction implements Function<CurrentDateTimeFunction.CurrentDateTimeRequest, JsonNode> {

    @Override
    public JsonNode apply(CurrentDateTimeRequest request) {
        log.info("Retrieving current date and time");
        try {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(request.getFormat());
            String formattedDateTime = now.format(formatter);
            log.info("Current date and time: {}", formattedDateTime);
            return JsonNodeFactory.instance.objectNode().put("currentDateTime", formattedDateTime);
        } catch (Exception e) {
            log.error("Error retrieving current date and time", e);
            return JsonNodeFactory.instance.objectNode().put("error", e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentDateTimeRequest {
        @NotBlank
        @JsonPropertyDescription("Required. Specifies the format in which the current date and time should be returned. Example: yyyy-MM-dd HH:mm:ss")
        private String format;
    }
}
