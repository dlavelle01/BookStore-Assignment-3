package com.ucd.bookshop.exception;

/**
 * Exception thrown when user registration process fails.
 * This can occur during user creation, customer creation, or any part of the registration workflow.
 */
public class UserRegistrationException extends RuntimeException {

    public UserRegistrationException(String message) {
        super(message);
    }

    public UserRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
} 