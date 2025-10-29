package com.springboot.FlomadAIplanner.exceptions.validators;

public class GuestMessageLimitReachedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public GuestMessageLimitReachedException(String message) {
        super(message);
    }
}

