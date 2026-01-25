package com.eventmanager.exception;

// Exception thrown when attempting to register for a full event
public class RegistrationFullException extends RuntimeException {

    public RegistrationFullException(String message) {
        super(message);
    }
}
