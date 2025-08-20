package com.ucd.bookshop.controllers.web;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ucd.bookshop.config.SecurityConfig.CustomUserDetails;
import com.ucd.bookshop.exception.BookNotFoundException;
import com.ucd.bookshop.exception.UserNotFoundException;
import com.ucd.bookshop.model.ShoppingCartWithInventory;
import com.ucd.bookshop.repository.ShoppingCartRepository;
import com.ucd.bookshop.service.CustomerCartService;

@Controller
@RequestMapping("/v1/web/customers")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerCartService customerCartService;

    private final ShoppingCartRepository shoppingCartRepository;

    @Autowired
    public CustomerController(CustomerCartService customerCartService, ShoppingCartRepository shoppingCartRepository) {
        this.customerCartService = customerCartService;
        this.shoppingCartRepository = shoppingCartRepository;
    }

    @GetMapping("/checkout")
    public String showCheckout(Model model) throws UserNotFoundException {
        Integer customerId = getCurrentCustomerId(); 

        List<ShoppingCartWithInventory> cartItems = shoppingCartRepository.findShoppingCartWithInventoryByCustomerId(customerId);
        model.addAttribute("cartItems", cartItems);

        return "customers/checkout";
    }

    @GetMapping("/addItemToCart")
    public String addItemToCart(Model model, @RequestParam Long bookId) throws UserNotFoundException, BookNotFoundException {
        Integer customerId = getCurrentCustomerId(); 

        List<ShoppingCartWithInventory> cartItems = customerCartService.addBookToCart(customerId, 1L, bookId);
        model.addAttribute("cartItems", cartItems);

        return "customers/checkout";
    }

    @GetMapping("/removeItemFromCart")
    public String removeItemFromCart(Model model, @RequestParam Long bookId) throws UserNotFoundException, BookNotFoundException {
        Integer customerId = getCurrentCustomerId(); 

        List<ShoppingCartWithInventory> cartItems = customerCartService.removeBookFromCart(customerId, bookId);
        model.addAttribute("cartItems", cartItems);

        return "customers/checkout";
    }

    @GetMapping("/order")
    public String makeOrder(Model model) throws UserNotFoundException {
        Integer customerId = getCurrentCustomerId(); 

        List<ShoppingCartWithInventory> cartItems = shoppingCartRepository.findShoppingCartWithInventoryByCustomerId(customerId);
        
        if (cartItems.isEmpty()) {
            model.addAttribute("errorMessage", "Your cart is empty.");
            return "customers/order";
        }

        BigDecimal orderTotal = cartItems.stream()
            .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getCopies())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("orderTotal", orderTotal);

        return "customers/order";
    }

    @PostMapping("/order")
    public String processOrder(@RequestParam String orderTotal, 
                              @RequestParam String creditCardNumber, 
                              Model model) throws UserNotFoundException {
        Integer customerId = getCurrentCustomerId();
        
        try {
            // Get cart items
            List<ShoppingCartWithInventory> cartItems = shoppingCartRepository.findShoppingCartWithInventoryByCustomerId(customerId);
            
            if (cartItems.isEmpty()) {
                model.addAttribute("errorMessage", "Your cart is empty.");
                return "customers/order";
            }
            
            customerCartService.removeCart(customerId);
            
            model.addAttribute("successMessage", "Order placed successfully! Total: $" + orderTotal);
            model.addAttribute("orderConfirmed", true);
            
            return "customers/order";
            
        } catch (Exception e) {
            logger.error("Error processing order for customer: {}", customerId, e);
            model.addAttribute("errorMessage", "Failed to process order. Please try again.");
            return "customers/order";
        }
    }

    /**
     * Get the current customer ID from the security context
     * 
     * @throws UserNotFoundException
     */
    private Integer getCurrentCustomerId() throws UserNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer customerId = userDetails.getCustomerId();

            if (customerId != null) {
                return customerId;
            } else {
                logger.warn("No customer ID found for user: {}", userDetails.getUsername());
                throw new UserNotFoundException(customerId, userDetails.getUsername());
            }
        } else {
            logger.error("No authenticated user found");
            throw new UserNotFoundException("No authenticated user found");
        }
    }
}