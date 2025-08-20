package com.ucd.bookshop.exception;

public class BookNotFoundException extends Exception {

    private final long id;

    public BookNotFoundException(long id) {
        super(String.format("Book is not found with id : '%s'", id));
        this.id = id;
    }

    public long getId() {
        return id;
    }
}