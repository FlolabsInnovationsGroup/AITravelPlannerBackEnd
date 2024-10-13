package com.springboot.FlomadAIplanner.exceptions.api;

/**
 * Custom exception for handling errors related to Google API.
 */
public class GoogleAPIException extends Exception {
    public GoogleAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
