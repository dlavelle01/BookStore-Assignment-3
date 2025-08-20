package com.ucd.bookshop.service;

import com.ucd.bookshop.constants.Role;
import com.ucd.bookshop.controllers.dto.UserRegistrationRequestDto;
import com.ucd.bookshop.model.Customer;
import com.ucd.bookshop.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
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
class UserRegistrationServiceTest {

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;

    @Test
    @Rollback
    void testRegisterUserWithCustomer() {
        // Arrange - Create registration request with clean business logic
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setUserName("john.doe");
        request.setPassword("hashed-password");
        request.setName("John");
        request.setSurname("Doe");
        request.setEmail("john@example.com");
        request.setPhoneNumber("1234567890");
        request.setAddress("123 Main Street");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));

        // Act - Use service layer for business operation
        UserRegistrationService.UserRegistrationResult result = userRegistrationService
                .registerUserWithCustomer(request);

        // Assert - Verify business rules are satisfied
        User createdUser = result.getUser();
        Customer createdCustomer = result.getCustomer();

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getUserId()).isNotNull();
        assertThat(createdUser.getUserName()).isEqualTo("john.doe");

        assertThat(createdCustomer).isNotNull();
        assertThat(createdCustomer.getId()).isNotNull();
        assertThat(createdCustomer.getUser().getUserId()).isEqualTo(createdUser.getUserId());
        assertThat(createdCustomer.getName()).isEqualTo("John");
        assertThat(createdCustomer.getEmail()).isEqualTo("john@example.com");

        // Verify bidirectional relationship works through service layer
        User fetchedUser = userService.getUserById(createdUser.getUserId());
        assertThat(fetchedUser.getCustomer()).isNotNull();
        assertThat(fetchedUser.getCustomer().getId()).isEqualTo(createdCustomer.getId());
    }

    @Test
    @Rollback
    void testCascadeDeleteUserDeletesCustomer() {
        // Arrange - Register user with customer using service
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setUserName("cascade.user");
        request.setPassword("pw");
        request.setName("Cascade");
        request.setSurname("Test");
        request.setEmail("cascade@example.com");
        request.setPhoneNumber("555-1234");
        request.setAddress("Cascade Street");
        request.setDateOfBirth(LocalDate.of(1980, 5, 5));

        UserRegistrationService.UserRegistrationResult result = userRegistrationService
                .registerUserWithCustomer(request);
        UUID userId = result.getUser().getUserId();
        Integer customerId = result.getCustomer().getId();

        // Act - Delete user through service
        userService.deleteUser(userId);

        // Assert - Customer should also be gone due to cascade
        assertThat(userService.findById(userId)).isEmpty();
        assertThat(customerService.findById(customerId)).isEmpty();
    }

    @Test
    @Rollback
    void testCreateSeparateUserAndCustomer() {
        // Arrange - Create user first
        UserRegistrationRequestDto userRequest = new UserRegistrationRequestDto();
        userRequest.setUserName("jane.doe");
        userRequest.setPassword("hashed-password");
        userRequest.setName("Jane");
        userRequest.setSurname("Doe");
        userRequest.setEmail("jane@example.com");
        userRequest.setPhoneNumber("0987654321");

        User createdUser = null;
        try {
            createdUser = userService.createUser(userRequest, Role.CUSTOMER);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Act - Create customer linked to existing user
        CustomerService.CreateCustomerRequest customerRequest = new CustomerService.CreateCustomerRequest(
                createdUser.getUserId(),
                "Jane",
                "Doe",
                "jane@example.com",
                "0987654321",
                "456 Oak Avenue",
                LocalDate.of(1985, 5, 15));

        Customer createdCustomer = customerService.createCustomer(customerRequest);

        // Assert - Verify relationship
        assertThat(createdCustomer.getUser().getUserId()).isEqualTo(createdUser.getUserId());

        User fetchedUser = userService.getUserById(createdUser.getUserId());
        assertThat(fetchedUser.getCustomer().getId()).isEqualTo(createdCustomer.getId());
    }
}