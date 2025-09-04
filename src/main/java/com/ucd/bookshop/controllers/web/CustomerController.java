package com.ucd.bookshop.controllers.web;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.ucd.bookshop.config.SecurityConfig.CustomUserDetails;
import com.ucd.bookshop.exception.BookNotFoundException;
import com.ucd.bookshop.exception.UserNotFoundException;
import com.ucd.bookshop.model.ShoppingCartWithInventory;
import com.ucd.bookshop.repository.ShoppingCartRepository;
import com.ucd.bookshop.service.CustomerCartService;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/v1/web/customers")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerCartService customerCartService;
    private final ShoppingCartRepository shoppingCartRepository;

    @Value("${stripe.publishableKey}")
    private String stripePublishableKey;

    @Value("${stripe.currency:EUR}")
    private String currency;

    @Autowired
    public CustomerController(CustomerCartService customerCartService,
                              ShoppingCartRepository shoppingCartRepository) {
        this.customerCartService = customerCartService;
        this.shoppingCartRepository = shoppingCartRepository;
    }

    private static long toMinorUnits(BigDecimal amount) {
        return amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private static String fmtCurrency(BigDecimal amount, String currencyCode) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.UK); // €/€ locale-independent
        try { nf.setCurrency(java.util.Currency.getInstance(currencyCode)); } catch (Exception ignored) {}
        return nf.format(amount);
    }

    /*
    @Autowired
    public CustomerController(CustomerCartService customerCartService, ShoppingCartRepository shoppingCartRepository) {
        this.customerCartService = customerCartService;
        this.shoppingCartRepository = shoppingCartRepository;
    }

     */

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
    @PreAuthorize("hasRole('CUSTOMER')")
    public String makeOrder(Model model) throws UserNotFoundException {
        Integer customerId = getCurrentCustomerId(); 

        List<ShoppingCartWithInventory> cartItems = shoppingCartRepository.findShoppingCartWithInventoryByCustomerId(customerId);
        
        if (cartItems.isEmpty()) {
            model.addAttribute("errorMessage", "Your cart is empty.");
            return "customers/order";
        }

        // ✅ Compute total server-side
        BigDecimal orderTotal = cartItems.stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getCopies())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("orderTotal", "€" + orderTotal);

        // Create a PaymentIntent for that total (type-safe params builder)
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(toMinorUnits(orderTotal))
                .setCurrency(currency.toLowerCase())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .putMetadata("customerId", customerId.toString())
                .build();

        try {
            PaymentIntent intent = PaymentIntent.create(params);
            model.addAttribute("stripePublishableKey", stripePublishableKey);
            model.addAttribute("stripeClientSecret", intent.getClientSecret());
        } catch (StripeException e) {
            logger.error("Stripe PaymentIntent creation failed", e);
            model.addAttribute("errorMessage", "Unable to start payment. Please try again.");
        }

        return "customers/order";
    }

    @GetMapping("/order/success")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String orderSuccess(
            @RequestParam(name = "payment_intent", required = false) String paymentIntentId,
            Model model) throws UserNotFoundException {
        if (paymentIntentId == null) {
            model.addAttribute("errorMessage", "Payment not found.");
            return "customers/order";
        }

        try {
            PaymentIntent pi = PaymentIntent.retrieve(paymentIntentId);

            if (!"succeeded".equalsIgnoreCase(pi.getStatus())) {
                model.addAttribute("errorMessage", "Payment not completed.");
                return "customers/order";
            }

            // Amount actually received (minor units -> BigDecimal)
            BigDecimal paid = new BigDecimal(pi.getAmountReceived()).movePointLeft(2);
            Integer customerId = getCurrentCustomerId();

            // ✅ Idempotently clear cart and (optionally) create an Order record.
            // If you later add an Order entity, store pi.getId() to prevent duplicates.
            customerCartService.removeCart(customerId);

            model.addAttribute("successMessage", "Payment successful! Total: €" + paid);
            model.addAttribute("orderConfirmed", true);

        } catch (Exception e) {
            logger.error("Error handling payment success", e);
            model.addAttribute("errorMessage", "We couldn’t confirm your payment. Please contact support.");
        }

        return "customers/order";
    }

    @GetMapping("/order/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String orderCancel(Model model) {
        model.addAttribute("errorMessage", "Payment cancelled.");
        return "customers/order";
    }
    /* Retire due to Strip addition
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
    */

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