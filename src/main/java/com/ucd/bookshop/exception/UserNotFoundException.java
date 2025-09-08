package com.ucd.bookshop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UserNotFoundException extends RuntimeException { // unchecked
    private final Integer id;

    public UserNotFoundException(String message) {
        super(message);
        this.id = null;
    }

    public UserNotFoundException(Integer id, String username) {
        super("User not found (customerId=" + id + ", username=" + username + ")");
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
