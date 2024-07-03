package com.springboot.FlomadAIplanner.handler;

import org.springframework.context.annotation.Description;
import org.springframework.http.HttpStatus;

@Description("A record that encapsulates details about an exception for communication between services and users. " +
        "This includes a message tailored for customer understanding, a detailed technical explanation for developers, " +
        "and the appropriate HTTP status code reflecting the nature of the error.")
public record ExceptionRecord(
        String customerErrorMessage,
        String technicalRuteCauseExceptionExplanationForDevelopers,
        HttpStatus httpStatus
) {
}
