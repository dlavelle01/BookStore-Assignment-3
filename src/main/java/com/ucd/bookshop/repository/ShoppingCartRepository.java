package com.ucd.bookshop.repository;

import com.ucd.bookshop.model.ShoppingCart;
import com.ucd.bookshop.model.ShoppingCartWithInventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    @Modifying
    @Transactional
    void deleteByCustomerId(Integer customerId);

    List<ShoppingCart> findByCustomerId(Integer customerId);

    Optional<ShoppingCart> findByCustomerIdAndBookIdAndAbandonedFalse(Integer customerId, Long bookId);

    /**
     * Get shopping cart items with inventory for  customer
     */
    @Query("""
        select new com.ucd.bookshop.model.ShoppingCartWithInventory(
            sc.id, sc.book, sc.customer, sc.abandoned, 
            COALESCE(SUM(i.copies), 0) * -1
        )
        from ShoppingCart sc 
        inner join Inventory i on i.book.id = sc.book.id and i.onHoldForCustomerId = sc.customer.id
        where sc.customer.id = :customerId and sc.abandoned = false
        group by sc.id, sc.book, sc.customer, sc.abandoned
        """)
    List<ShoppingCartWithInventory> findShoppingCartWithInventoryByCustomerId(@Param("customerId") Integer customerId);

} 