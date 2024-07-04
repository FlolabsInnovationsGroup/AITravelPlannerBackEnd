package com.springboot.FlomadAIplanner.exceptions;

public class GoogleAPIException extends Exception {
    public GoogleAPIException(String message) {
        super(message);
    }

    public GoogleAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
