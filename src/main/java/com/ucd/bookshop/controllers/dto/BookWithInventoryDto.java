package com.ucd.bookshop.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.ucd.bookshop.model.BookInventory;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

public class BookWithInventoryDto {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 2047, message = "Title must be between 3 and 2047 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(min = 5, max = 1024, message = "Author must be between 5 and 1023 characters")
    private String author;

    @NotBlank(message = "ISBN is required")
    @Size(min = 3, max = 511, message = "ISBN must be between 3 and 511 characters")
    private String isbn;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @NotNull(message = "Year is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate year;

    @NotNull(message = "Copies is required")
    private Long copies;

    public BookWithInventoryDto() {
        super();
        this.copies = 0L;
    }

    public BookWithInventoryDto(BookInventory bookInventory) {
        this.id = bookInventory.getId();
        this.title = bookInventory.getTitle();
        this.author = bookInventory.getAuthor();
        this.isbn = bookInventory.getIsbn();
        this.price = bookInventory.getPrice();
        this.year = bookInventory.getYear();
        this.copies = bookInventory.getCopies();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getYear() {
        return year;
    }

    public void setYear(LocalDate year) {
        this.year = year;
    }

    public Long getCopies() {
        return copies;
    }

    public void setCopies(Long copies) {
        this.copies = copies;
    }
}