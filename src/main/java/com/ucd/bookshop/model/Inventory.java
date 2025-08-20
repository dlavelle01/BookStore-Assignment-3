package com.ucd.bookshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @NotNull
    @Column(name = "copies")
    private Integer copies;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "on_hold_for_customer_id")
    private Integer onHoldForCustomerId;

    // Constructors
    public Inventory() {
        super();
    }

    public Inventory(Book book, Integer copies, Integer onHoldForCustomerId) {
        this.book = book;
        this.copies = copies;
        this.onHoldForCustomerId = onHoldForCustomerId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Integer getCopies() {
        return copies;
    }

    public void setCopies(Integer copies) {
        this.copies = copies;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getOnHoldForCustomerId() {
        return onHoldForCustomerId;
    }

    public void setOnHoldForCustomerId(Integer onHoldForCustomerId) {
        this.onHoldForCustomerId = onHoldForCustomerId;
    }
}