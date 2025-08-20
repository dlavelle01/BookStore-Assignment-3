package com.ucd.bookshop.service;

import com.ucd.bookshop.controllers.dto.BookWithInventoryDto;
import com.ucd.bookshop.exception.BookAlreadyExistsException;
import com.ucd.bookshop.exception.BookNotFoundException;
import com.ucd.bookshop.model.Book;
import com.ucd.bookshop.model.BookInventory;
import com.ucd.bookshop.model.Inventory;
import com.ucd.bookshop.repository.BookRepository;
import com.ucd.bookshop.repository.InventoryRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookInventoryService {

    private final BookRepository bookRepository;

    private final InventoryRepository inventoryRepository;

    @Autowired
    public BookInventoryService(BookRepository bookRepository, InventoryRepository inventoryRepository) {
        this.bookRepository = bookRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Get all books with their total copies
     */
    public List<BookWithInventoryDto> getAllBooksWithInventory() {
        List<BookInventory> results = bookRepository.findAllBooksWithInventory();
        return results.stream()
                .map(BookWithInventoryDto::new)
                .toList();
    }

    /**
     * Get a specific book with its total copies
     */
    public BookWithInventoryDto getBookWithInventoryById(Long bookId) throws BookNotFoundException {
        BookInventory result = bookRepository.findBookWithInventoryById(bookId);
        if (result == null) {
            throw new BookNotFoundException(bookId);
        }

        return new BookWithInventoryDto(result);
    }

    /**
     * Update the inventory for a book.
     * 
     * @Notes copies can be negative to indicate a book is on hold or sold.
     * 
     * TODO: Remove customerId this is for admin only
     */
    @Transactional
    public BookWithInventoryDto updateBookInventory(BookWithInventoryDto bookWithInventoryDto, Integer customerId)
            throws BookNotFoundException {
        BookInventory bookInventory = bookRepository.findBookWithInventoryById(bookWithInventoryDto.getId());
        if (bookInventory == null) {
            throw new BookNotFoundException(bookWithInventoryDto.getId());
        }

        // Update the book inventory
        Long currentCopies = bookInventory.getCopies();
        Long copies = bookWithInventoryDto.getCopies();
       
        long difference = copies - currentCopies;
       
        // Get the actual Book from db
        Book book = bookRepository.findById(bookWithInventoryDto.getId())
                .orElseThrow(() -> new BookNotFoundException(bookWithInventoryDto.getId()));
        
        // Update the book
        book.setTitle(bookWithInventoryDto.getTitle());
        book.setAuthor(bookWithInventoryDto.getAuthor());
        book.setIsbn(bookWithInventoryDto.getIsbn());
        book.setPrice(bookWithInventoryDto.getPrice());
        book.setYear(bookWithInventoryDto.getYear());
        
        // Save the the details
        bookRepository.save(book);
        
        // Create inventory change 
        Inventory inventory = new Inventory(book, (int) difference, customerId);
        inventoryRepository.save(inventory);

        bookInventory.setCopies(copies);

        return new BookWithInventoryDto(bookInventory);
    }

    @Transactional
    public BookWithInventoryDto updateBookInventory(Book book, Integer quantity, Integer customerId)
            throws BookNotFoundException {
        BookInventory bookInventory = bookRepository.findBookWithInventoryById(book.getId());
        if (bookInventory == null) {
            throw new BookNotFoundException(book.getId());
        }

        // Update the book inventory
        Long currentCopies = bookInventory.getCopies();
        Long copies = quantity.longValue();
       
        long difference = copies - currentCopies;
       
        // Create inventory change 
        Inventory inventory = new Inventory(book, (int) difference, customerId);
        inventoryRepository.save(inventory);

        bookInventory.setCopies(copies);

        return new BookWithInventoryDto(bookInventory);
    }

    @Transactional
    public void updateBookInventoryForCustomer(Book book, Integer quantity, Integer customerId){
       
        // Create inventory change 
        Inventory inventory = new Inventory(book,  quantity * -1, customerId);
        inventoryRepository.save(inventory);
    }

    @Transactional
    public BookWithInventoryDto createBookInventory(BookWithInventoryDto bookWithInventoryDto, Integer customerId)
            throws BookAlreadyExistsException {

        // Corner case
        if (bookWithInventoryDto.getId() != null && bookWithInventoryDto.getId() != 0) {
            throw new BookAlreadyExistsException(bookWithInventoryDto.getId());
        }

        Book book = new Book(null, bookWithInventoryDto.getTitle(), bookWithInventoryDto.getAuthor(),
                bookWithInventoryDto.getIsbn(), bookWithInventoryDto.getPrice(), bookWithInventoryDto.getYear());
        bookRepository.save(book);

        Inventory inventory = new Inventory(book,
                bookWithInventoryDto.getCopies() != null ? bookWithInventoryDto.getCopies().intValue() : 0, customerId);
        inventoryRepository.save(inventory);

        return new BookWithInventoryDto(new BookInventory(book, bookWithInventoryDto.getCopies()));
    }

    @Transactional
    public void deleteBookInventory(Long bookId) throws BookNotFoundException {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));

        // Cascaded at database level
        bookRepository.delete(book);
    }

    public Book getBookById(Long bookId) throws BookNotFoundException {
        return bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
    }
}