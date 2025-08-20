package com.ucd.bookshop.repository;

import com.ucd.bookshop.model.Customer;
import com.ucd.bookshop.model.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)  // <-- Force use of H2
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=password",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.defer-datasource-initialization=false",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.sql.init.mode=never"
})
@Transactional
public class UserCustomerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @Rollback
    void testCreateUserAndCustomer() {
        // Create user
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("john.doe");
        user.setPassword("hashed-password");
        user.setSalt("somesalt");
        user.setRoleId(1);
        User savedUser = userRepository.save(user);

        // Create customer linked to user
        Customer customer = new Customer();
        customer.setUser(savedUser);
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setEmail("john@example.com");
        customer.setPhoneNumber("1234567890");
        customer.setAddress("123 Main Street");
        customer.setDateOfBirth(LocalDate.of(1990, 1, 1));
        Customer savedCustomer = customerRepository.save(customer);

        // Verify the customer -> user relationship
        Customer fetchedCustomer = customerRepository.findById(savedCustomer.getId()).orElseThrow();
        assertThat(fetchedCustomer.getUser().getUserId()).isEqualTo(savedUser.getUserId());

        // For bidirectional verification, we need to refresh the user from the database
        User fetchedUser = userRepository.findById(savedUser.getUserId()).orElseThrow();
        assertThat(fetchedUser.getCustomer()).isNotNull();
        assertThat(fetchedUser.getCustomer().getId()).isEqualTo(savedCustomer.getId());
    }

    @Test
    @Rollback
    void testCascadeDeleteUserDeletesCustomer() {
        UUID userId = UUID.randomUUID();

        // Create and save user and customer
        User user = new User();
        user.setUserId(userId);
        user.setUserName("cascade.user");
        user.setPassword("pw");
        user.setSalt("salt");
        user.setRoleId(1);
        User savedUser = userRepository.save(user);

        Customer customer = new Customer();
        customer.setUser(savedUser);
        customer.setName("Cascade");
        customer.setSurname("Test");
        customer.setEmail("cascade@example.com");
        customer.setPhoneNumber("555-1234");
        customer.setAddress("Cascade Street");
        customer.setDateOfBirth(LocalDate.of(1980, 5, 5));
        Customer savedCustomer = customerRepository.save(customer);

        // Store customer ID for later verification
        Integer customerId = savedCustomer.getId();

        // Load user with its customer relationship to trigger cascade delete
        User userToDelete = userRepository.findById(userId).orElseThrow();
        userRepository.delete(userToDelete);

        // Flush to apply delete
        userRepository.flush();

        // Customer should also be gone (due to CascadeType.ALL on User)
        Optional<Customer> deletedCustomer = customerRepository.findById(customerId);
        assertThat(deletedCustomer).isEmpty();
    }

    @Test
    @Rollback
    void testCustomerWithoutUserFails() {
        Customer customer = new Customer();
        customer.setUser(null); // No user
        customer.setName("NoUser");
        customer.setSurname("Error");
        customer.setEmail("nouser@example.com");
        customer.setPhoneNumber("000-0000");
        customer.setAddress("Nowhere");
        customer.setDateOfBirth(LocalDate.of(1970, 1, 1));

        assertThrows(DataIntegrityViolationException.class, () -> {
            customerRepository.saveAndFlush(customer);
        });
    }
}
