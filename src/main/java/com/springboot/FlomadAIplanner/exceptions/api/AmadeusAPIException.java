package com.springboot.FlomadAIplanner.exceptions.api;


public class AmadeusAPIException extends Exception {
    public AmadeusAPIException(String message) {
        super(message);
    }

    public AmadeusAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
