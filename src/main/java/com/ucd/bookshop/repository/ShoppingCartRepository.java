package com.ucd.bookshop.repository;

import com.ucd.bookshop.model.ShoppingCart;
import com.ucd.bookshop.model.ShoppingCartWithInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    // Delete all cart rows for a customer
    void deleteByCustomer_Id(Integer customerId);

    // Find all cart rows for a customer
    List<ShoppingCart> findByCustomer_Id(Integer customerId);

    // Find specific row for this customer+book, only active (not abandoned)
    Optional<ShoppingCart> findByCustomer_IdAndBook_IdAndAbandonedFalse(Integer customerId, Long bookId);

    // Public ID lookups
    Optional<ShoppingCart> findByPublicId(String publicId);
    Optional<ShoppingCart> findByPublicIdAndCustomer_Id(String publicId, Integer customerId);

    // Projection for checkout view — uses your (ShoppingCart, Long) ctor
    @Query("""
       select new com.ucd.bookshop.model.ShoppingCartWithInventory(
           sc,
           COALESCE(SUM(i.copies), 0L) * -1L
       )
       from ShoppingCart sc
       join Inventory i
            on i.book.id = sc.book.id
           and i.onHoldForCustomerId = sc.customer.id
       where sc.customer.id = :customerId
         and sc.abandoned = false
       group by sc
    """)
    List<ShoppingCartWithInventory> findShoppingCartWithInventoryByCustomerId(
            @Param("customerId") Integer customerId);


}
