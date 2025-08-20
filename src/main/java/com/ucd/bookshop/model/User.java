package com.ucd.bookshop.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "`user`")
public class User {

    @Id
    @Column(name = "user_id", columnDefinition = "CHAR(36)", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String salt;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "last_modified_by")
    private Integer lastModifiedBy;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime lastModifiedDate;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
    private Customer customer;

    @PrePersist
    public void generateId() {
        if (userId == null) {
            userId = UUID.randomUUID();
        }
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Integer getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(Integer lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}


