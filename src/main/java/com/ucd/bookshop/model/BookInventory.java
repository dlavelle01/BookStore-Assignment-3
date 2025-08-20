package com.ucd.bookshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * POJO for book with inventory.
 */
public class BookInventory extends Book {

    private Long copies;

    public BookInventory(Long id, String title, String author, String isbn, BigDecimal price, LocalDate year, Long copies) {
        super(id, title, author, isbn, price, year);
        this.copies = copies;
    }

    public BookInventory(Book book, Long copies) {
        super(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(), book.getPrice(), book.getYear());
        this.copies = copies;
    }   

    public Long getCopies() {
        return copies;
    }

    public void setCopies(Long copies) {
        this.copies = copies;
    }

    public Book toBook() {
        return new Book(getId(), getTitle(), getAuthor(), getIsbn(), getPrice(), getYear());
    }
}
