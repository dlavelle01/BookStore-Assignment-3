package com.ucd.bookshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "shopping_cart")
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shopping_cart_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @NotNull
    @Column(name = "abandoned")
    private Boolean abandoned = false;

    // Constructors
    public ShoppingCart() {
        super();
    }

    public ShoppingCart(Book book, Customer customer, Boolean abandoned) {
        this.book = book;
        this.customer = customer;
        this.abandoned = abandoned != null ? abandoned : false;
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getAbandoned() {
        return abandoned;
    }

    public void setAbandoned(Boolean abandoned) {
        this.abandoned = abandoned;
    }

    @Override
    public String toString() {
        return "ShoppingCart{" +
                "id=" + id +
                ", bookId=" + (book != null ? book.getId() : null) +
                ", customerId=" + (customer != null ? customer.getId() : null) +
                ", createdDate=" + createdDate +
                ", abandoned=" + abandoned +
                '}';
    }
}