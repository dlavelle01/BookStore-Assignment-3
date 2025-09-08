package com.ucd.bookshop.service;

import com.ucd.bookshop.constants.Role;
import com.ucd.bookshop.controllers.dto.UserRegistrationRequestDto;
import com.ucd.bookshop.exception.UserRegistrationException;
import com.ucd.bookshop.model.Customer;
import com.ucd.bookshop.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserRegistrationService {

    private final UserService userService;
    private final CustomerService customerService;

    @Autowired
    public UserRegistrationService(UserService userService, CustomerService customerService) {
        this.userService = userService;
        this.customerService = customerService;
    }

    public UserRegistrationResult registerUserWithCustomer(UserRegistrationRequestDto request) throws UserRegistrationException {

        try {
            // Create user first
            User createdUser = userService.createUser(request, Role.CUSTOMER);

            CustomerService.CreateCustomerRequest customerRequest = new CustomerService.CreateCustomerRequest(
                    createdUser.getUserId(),
                    //createdUser.getIsUsing2FA(),
                    request.getName(),
                    request.getSurname(),
                    request.getEmail(),
                    request.getPhoneNumber(),
                    request.getAddress(),
                    request.getDateOfBirth());

            Customer createdCustomer = customerService.createCustomer(customerRequest);
            return new UserRegistrationResult(createdUser, createdCustomer);
        } catch (Exception e) {
            throw new UserRegistrationException(e.getMessage());
        }
    }

    public static class UserRegistrationResult {
        private final User user;
        private final Customer customer;

        public UserRegistrationResult(User user, Customer customer) {
            this.user = user;
            this.customer = customer;
        }

        public User getUser() {
            return user;
        }

        public Customer getCustomer() {
            return customer;
        }
    }
}