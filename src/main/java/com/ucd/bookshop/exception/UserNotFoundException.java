package com.ucd.bookshop.exception;

public class UserNotFoundException extends Exception {

    private final Integer id;

    public UserNotFoundException(Integer id, String username) {
        super(String.format("User is not found with id : '%s' for user : '%s'", id, username));
        this.id = id;
    }

    public UserNotFoundException(String message) {
        super(message);
        this.id = null;
    }

    public Integer getId() {
        return id;
    }
}
