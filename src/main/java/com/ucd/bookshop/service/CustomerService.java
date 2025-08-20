package com.ucd.bookshop.service;

import com.ucd.bookshop.model.Customer;
import com.ucd.bookshop.model.User;
import com.ucd.bookshop.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserService userService;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, UserService userService) {
        this.customerRepository = customerRepository;
        this.userService = userService;
    }

    public Customer createCustomer(CreateCustomerRequest request) {
        User user = userService.getUserById(request.getUserId());

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setName(request.getName());
        customer.setSurname(request.getSurname());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        customer.setDateOfBirth(request.getDateOfBirth());

        return customerRepository.save(customer);
    }

    public Optional<Customer> findById(Integer customerId) {
        return customerRepository.findById(customerId);
    }

    public Customer getCustomerById(Integer customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
    }

    public void deleteCustomer(Integer customerId) {
        Customer customer = getCustomerById(customerId);
        customerRepository.delete(customer);
    }

    public static class CreateCustomerRequest {
        private UUID userId;
        private String name;
        private String surname;
        private String email;
        private String phoneNumber;
        private String address;
        private LocalDate dateOfBirth;

        public CreateCustomerRequest() {
        }

        public CreateCustomerRequest(UUID userId, String name, String surname, String email,
                String phoneNumber, String address, LocalDate dateOfBirth) {
            this.userId = userId;
            this.name = name;
            this.surname = surname;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.address = address;
            this.dateOfBirth = dateOfBirth;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSurname() {
            return surname;
        }

        public void setSurname(String surname) {
            this.surname = surname;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }
    }
}