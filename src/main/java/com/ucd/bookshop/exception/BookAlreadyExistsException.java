package com.ucd.bookshop.exception;

public class BookAlreadyExistsException extends Exception {

    private final long id;

    public BookAlreadyExistsException(long id) {
        super(String.format("Book already exists with id : '%s'", id));
        this.id = id;
    }
}
