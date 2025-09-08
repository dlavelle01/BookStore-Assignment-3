package com.ucd.bookshop.repository;

import com.ucd.bookshop.model.Customer;
import com.ucd.bookshop.model.ShoppingCartWithInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;



import java.util.Optional;
import org.springframework.data.repository.*;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    @Query("select c.id from Customer c where c.user.userName = :username")
    Optional<Integer> findCustomerIdByUsername(@Param("username") String username);

    // (optional convenience if you ever need the entity)
    Optional<Customer> findByUser_UserName(String userName);
}

