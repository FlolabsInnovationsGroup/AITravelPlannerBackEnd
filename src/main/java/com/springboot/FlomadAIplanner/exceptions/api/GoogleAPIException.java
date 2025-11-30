package com.springboot.FlomadAIplanner.exceptions.api;

/**
 * Custom exception for handling errors related to Google API.
 */
public class GoogleAPIException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public GoogleAPIException(String message) {
        super(message);
    }

    public GoogleAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
