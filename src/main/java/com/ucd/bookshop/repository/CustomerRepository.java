package com.ucd.bookshop.repository;

import com.ucd.bookshop.model.Customer;
import com.ucd.bookshop.model.ShoppingCartWithInventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    @Query("""
        select new com.ucd.bookshop.model.ShoppingCartWithInventory(
            sc.id, sc.book, sc.customer, sc.abandoned, 
            COALESCE(SUM(i.copies), 0)
        )
        from ShoppingCart sc 
        left join Inventory i on i.book.id = sc.book.id 
        group by sc.id, sc.book, sc.customer, sc.createdDate, sc.abandoned
        """)
    List<ShoppingCartWithInventory> findAllShoppingCartsWithInventory();
}
