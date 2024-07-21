package com.springboot.FlomadAIplanner.exceptions.validators;

public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
