package com.springboot.FlomadAIplanner.exceptions;

public class ViatorAPIException extends RuntimeException {

    public ViatorAPIException(String message) {
        super(message);
    }

    public ViatorAPIException(String message, Throwable cause) {
        super(message, cause);
    }

}
