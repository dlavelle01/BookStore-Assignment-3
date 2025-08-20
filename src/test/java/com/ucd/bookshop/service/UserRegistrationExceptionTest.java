package com.ucd.bookshop.service;

import com.ucd.bookshop.controllers.dto.UserRegistrationRequestDto;
import com.ucd.bookshop.exception.UserRegistrationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegistrationExceptionTest {

    @Mock
    private UserService userService;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    @Test
    void testRegisterUserWithCustomer_ThrowsUserRegistrationException_WhenUserCreationFails() {
        // Arrange
        UserRegistrationRequestDto request = createValidRegistrationRequest();
        
        // Mock userService to throw an exception
        try {
            when(userService.createUser(any(UserRegistrationRequestDto.class), any()))
                .thenThrow(new RuntimeException("Database connection failed"));
        } catch (Exception e) {
            fail("Mock setup should not throw exception");
        }

        // Act & Assert
        UserRegistrationException exception = assertThrows(
            UserRegistrationException.class,
            () -> userRegistrationService.registerUserWithCustomer(request)
        );

        assertNotNull(exception);
        assertEquals("Database connection failed", exception.getMessage());
    }

    @Test
    void testRegisterUserWithCustomer_ThrowsUserRegistrationException_WhenCustomerCreationFails() {
        // Arrange
        UserRegistrationRequestDto request = createValidRegistrationRequest();
        
        // Mock successful user creation but failed customer creation
        try {
            when(userService.createUser(any(UserRegistrationRequestDto.class), any()))
                .thenReturn(createMockUser());
        } catch (Exception e) {
            fail("Mock setup should not throw exception");
        }
        
        when(customerService.createCustomer(any(CustomerService.CreateCustomerRequest.class)))
            .thenThrow(new RuntimeException("Customer validation failed"));

        // Act & Assert
        UserRegistrationException exception = assertThrows(
            UserRegistrationException.class,
            () -> userRegistrationService.registerUserWithCustomer(request)
        );

        assertNotNull(exception);
        assertEquals("Customer validation failed", exception.getMessage());
    }

    @Test
    void testRegisterUserWithCustomer_ThrowsUserRegistrationException_WhenNullRequestProvided() {
        // Act & Assert
        assertThrows(
            UserRegistrationException.class,
            () -> userRegistrationService.registerUserWithCustomer(null)
        );
    }

    @Test
    void testRegisterUserWithCustomer_ThrowsUserRegistrationException_WhenInvalidDataProvided() {
        // Arrange - Create request with invalid data
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        // Intentionally leave fields null/empty to trigger validation errors
        
        try {
            when(userService.createUser(any(UserRegistrationRequestDto.class), any()))
                .thenThrow(new IllegalArgumentException("Username cannot be null"));
        } catch (Exception e) {
            fail("Mock setup should not throw exception");
        }

        // Act & Assert
        UserRegistrationException exception = assertThrows(
            UserRegistrationException.class,
            () -> userRegistrationService.registerUserWithCustomer(request)
        );

        assertNotNull(exception);
        assertEquals("Username cannot be null", exception.getMessage());
    }

    private UserRegistrationRequestDto createValidRegistrationRequest() {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setUserName("testuser");
        request.setPassword("password123");
        request.setName("Test");
        request.setSurname("User");
        request.setEmail("test@example.com");
        request.setPhoneNumber("1234567890");
        request.setAddress("123 Test Street");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        return request;
    }

    private com.ucd.bookshop.model.User createMockUser() {
        com.ucd.bookshop.model.User user = new com.ucd.bookshop.model.User();
        user.setUserName("testuser");
        user.setPassword("hashedpassword");
        user.setSalt("testsalt");
        user.setRoleId(2); // CUSTOMER role
        return user;
    }
} 