package com.ucd.bookshop.repository;

import com.ucd.bookshop.model.Inventory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
        
    /**
     * Get total copies for a specific book
     */
    @Query("select coalesce(sum(i.copies), 0) from Inventory i where i.book.id = :bookId")
    Long getTotalCopiesByBookId(Long bookId);


    List<Inventory> findAllByBookId(Long bookId);
} 