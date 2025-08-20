package com.ucd.bookshop.controllers.v1;

import com.ucd.bookshop.constants.Role;
import com.ucd.bookshop.controllers.dto.UserRegistrationRequestDto;
import com.ucd.bookshop.service.UserRegistrationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/api/registration")
@Tag(name = "User Registration", description = "User registration API")
@SecurityRequirement(name = "basicAuth")
public class UserRegistrationApiController {

    private final UserRegistrationService userRegistrationService;

    @Autowired
    public UserRegistrationApiController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @Operation(summary = "Register a new user", description = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequestDto request,
            BindingResult bindingResult) {

        // Run custom validation (business rules, uniqueness checks)
        DataBinder binder = new DataBinder(request);
        binder.validate();
        bindingResult.addAllErrors(binder.getBindingResult());

        // If validation errors exist, return them
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation failed");
            errorResponse.put("errors", bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            error -> error.getDefaultMessage(),
                            (existing, replacement) -> existing + "; " + replacement)));
                            
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            // Convert to service request
            UserRegistrationRequestDto serviceRequest = new UserRegistrationRequestDto();
            serviceRequest.setUserName(request.getUserName());
            serviceRequest.setPassword(request.getPassword());
            serviceRequest.setName(request.getName());
            serviceRequest.setSurname(request.getSurname());
            serviceRequest.setDateOfBirth(request.getDateOfBirth());
            serviceRequest.setAddress(request.getAddress());
            serviceRequest.setEmail(request.getEmail());
            serviceRequest.setPhoneNumber(request.getPhoneNumber());

            var result = userRegistrationService.registerUserWithCustomer(serviceRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", result.getUser().getUserId());
            response.put("customerId", result.getCustomer().getId());
            response.put("message", "Customer account registered successfully");
            response.put("role", Role.CUSTOMER.getName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Registration failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}