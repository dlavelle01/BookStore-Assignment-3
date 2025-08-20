package com.ucd.bookshop.controllers.v1;

import com.ucd.bookshop.controllers.dto.BookWithInventoryDto;
import com.ucd.bookshop.exception.BookAlreadyExistsException;
import com.ucd.bookshop.exception.BookNotFoundException;
import com.ucd.bookshop.service.BookInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api")
@Tag(name = "Books", description = "Book management Api")
@SecurityRequirement(name = "basicAuth")
public class BookApiController {

    private final BookInventoryService bookInventoryService;

    @Autowired
    public BookApiController( BookInventoryService bookInventoryService) {
        this.bookInventoryService = bookInventoryService;
    }

    @Operation(summary = "Get all books", description = "Retrieve a list of books")
    @GetMapping("/books")
    public List<BookWithInventoryDto> getAllBooks() {
        return bookInventoryService.getAllBooksWithInventory();
    }

    @Operation(summary = "Create a new book", description = "Add a new book ")
    @PostMapping("/books")
    public BookWithInventoryDto newBook(@Valid @RequestBody BookWithInventoryDto newBook) throws BookAlreadyExistsException {
        // TODO: Add customer id from security context
        return bookInventoryService.createBookInventory(newBook, null);
    }

    @Operation(summary = "Get book by ID", description = "Retrieve a specific book")
    @GetMapping("/books/{id}")
    public BookWithInventoryDto getBookById(@Parameter(description = "ID of the book to retrieve") @PathVariable(value = "id") Long bookId) throws BookNotFoundException {
        return bookInventoryService.getBookWithInventoryById(bookId);
    }

    @Operation(summary = "Update an existing book", description = "Update book details")
    @PutMapping("/books/{id}")
    public BookWithInventoryDto updateBook(@Parameter(description = "ID of the book to update") @PathVariable(value = "id") Long bookId, 
                          @Valid @RequestBody BookWithInventoryDto bookDetails) throws BookNotFoundException {
        // TODO: Add customer id from security context
        return bookInventoryService.updateBookInventory(bookDetails, null);
    }

    @Operation(summary = "Delete a book", description = "Remove a book")
    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBook(@Parameter(description = "ID of the book to delete") @PathVariable(value = "id") Long bookId) throws BookNotFoundException {
        bookInventoryService.deleteBookInventory(bookId);
        return ResponseEntity.ok().build();
    }
}
