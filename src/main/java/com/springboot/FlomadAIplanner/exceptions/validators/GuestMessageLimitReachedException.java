package com.springboot.FlomadAIplanner.exceptions.validators;

public class GuestMessageLimitReachedException extends RuntimeException {
    public GuestMessageLimitReachedException(String message) {
        super(message);
    }
}

