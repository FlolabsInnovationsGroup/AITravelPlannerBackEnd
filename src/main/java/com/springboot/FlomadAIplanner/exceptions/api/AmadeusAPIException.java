package com.springboot.FlomadAIplanner.exceptions.api;


public class AmadeusAPIException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public AmadeusAPIException(String message) {
        super(message);
    }

    public AmadeusAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
