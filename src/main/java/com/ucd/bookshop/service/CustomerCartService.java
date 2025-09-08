package com.ucd.bookshop.service;

import com.ucd.bookshop.exception.BookNotFoundException;
import com.ucd.bookshop.model.Book;
import com.ucd.bookshop.model.Customer;
import com.ucd.bookshop.model.ShoppingCart;
import com.ucd.bookshop.model.ShoppingCartWithInventory;
import com.ucd.bookshop.repository.CustomerRepository;
import com.ucd.bookshop.repository.ShoppingCartRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final CustomerRepository customerRepository;
    private final BookInventoryService bookInventoryService;

    @Autowired
    public CustomerCartService(ShoppingCartRepository shoppingCartRepository,
            CustomerRepository customerRepository,
            BookInventoryService bookInventoryService) {

        this.shoppingCartRepository = shoppingCartRepository;
        this.customerRepository = customerRepository;
        this.bookInventoryService = bookInventoryService;
    }

    @Transactional
    public List<ShoppingCartWithInventory> addBookToCart(Integer customerId, Long quantity, Long bookId)
            throws BookNotFoundException {

        // Verify customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        // Verify book exists
        Book book = bookInventoryService.getBookById(bookId);

       Optional<ShoppingCart> shoppingCart = shoppingCartRepository
               .findByCustomer_IdAndBook_IdAndAbandonedFalse(customerId, bookId);

        // Add the item if not present
        if (!shoppingCart.isPresent()) {
            ShoppingCart newCartItem = new ShoppingCart(book, customer, false);
            shoppingCartRepository.save(newCartItem);
        }

        // Update the inventory
        bookInventoryService.updateBookInventoryForCustomer(book, quantity.intValue(), customerId);

        return shoppingCartRepository.findShoppingCartWithInventoryByCustomerId(customerId);
    }

    @Transactional
    public List<ShoppingCartWithInventory> removeBookFromCart(Integer customerId, Long bookId)
            throws BookNotFoundException {

        List<ShoppingCartWithInventory> cartItems = shoppingCartRepository
                .findShoppingCartWithInventoryByCustomerId(customerId);

        ShoppingCartWithInventory shoppingCart = cartItems.stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException(bookId));

        ShoppingCart shoppingCartItem = shoppingCartRepository.findById(shoppingCart.getId())
                .orElseThrow(() -> new BookNotFoundException(bookId));

        shoppingCartRepository.delete(shoppingCartItem);

        bookInventoryService.updateBookInventoryForCustomer(shoppingCart.getBook(),
                shoppingCart.getCopies().intValue() * -1, customerId);

        cartItems.remove(shoppingCart);

        return cartItems;
    }

    @Transactional
    public List<ShoppingCartWithInventory> removeCart(Integer customerId) {

        List<ShoppingCartWithInventory> cartItems = shoppingCartRepository
                .findShoppingCartWithInventoryByCustomerId(customerId);

        shoppingCartRepository.deleteByCustomer_Id(customerId);

        return cartItems;
    }
}