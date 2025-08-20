package com.ucd.bookshop.repository;

import com.ucd.bookshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    User findByUserName(String username);

    @Query(value = "select c.customer_id from `user` u left join customer c on c.user_id = u.user_id where u.user_name = :username", nativeQuery = true)
    Integer findCustomerIdByUserName(String username);
}