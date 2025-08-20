package com.ucd.bookshop.controllers.web;

import com.ucd.bookshop.exception.BookAlreadyExistsException;
import com.ucd.bookshop.exception.BookNotFoundException;
import com.ucd.bookshop.service.BookInventoryService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.ucd.bookshop.controllers.dto.BookWithInventoryDto;

import java.util.List;

@Controller
@RequestMapping("/v1/web/books")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);
    
    private final BookInventoryService bookInventoryService;
    
    @Autowired
    public BookController(BookInventoryService bookInventoryService) {
        this.bookInventoryService = bookInventoryService;
    }

    // Get All Books
    @GetMapping({ "/" })
    public String getAllBooks(Model model) {
        List<BookWithInventoryDto> listBooks = bookInventoryService.getAllBooksWithInventory()
            .stream()
            .toList();

        model.addAttribute("listBooks", listBooks);

        // TODO: Change to books
        return "admin/home";
    }

    @GetMapping("/edit/{id}")
    public String showHomeWithEdit(@PathVariable("id") Long bookId, Model model) {
        
        /**
         * Gives the apperance nothing has changed in the background to make it look like a one page application
         * in-efficient but it is a simple solution to the problem
         */
        List<BookWithInventoryDto> listBooks = bookInventoryService.getAllBooksWithInventory()
            .stream()
            .toList();

                
        model.addAttribute("listBooks", listBooks);
        
        if (bookId == 0) {
            // Create new book
            simulateSinglePageApp(model, new BookWithInventoryDto(), true, true);
        } else {
            // Edit existing book
            try {
                var book = bookInventoryService.getBookWithInventoryById(bookId);
                simulateSinglePageApp(model, book, true, false);
               
            } catch (Exception e) {
                // If book not found, just show the home page
                return "redirect:/v1/web/books";
            }
        }
        
        return "admin/home";
    }

   

    @RequestMapping("/new")
    public String createBook(Model model) {
        model.addAttribute("book", new BookWithInventoryDto());
        return "admin/addBook";
    }

    // Create a new Book
    @PostMapping("/")
    public String newBook(@Valid @ModelAttribute("book") BookWithInventoryDto book, BindingResult bindingResult, Model model,
                         @RequestParam(value = "source", required = false) String source) {

        logger.info("newBook called with book ID: {}, title: '{}', author: '{}'", 
        book.getId(), book.getTitle(), book.getAuthor());

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors found for book ID: {}, errors: {}", 
                       book.getId(), bindingResult.getFieldErrors());
            
            // TODO: revist source logic
            if ("home".equals(source)) {
                simulateSinglePageApp(model, true, true);
                // book object with errors is already in the model from @ModelAttribute
                return "admin/home";
            }
            
            return "admin/addBook";
        }

        // TODO: add customerId from security context
        try {
            bookInventoryService.createBookInventory(book, null);
        } catch (BookAlreadyExistsException e) {
            logger.error("Book with ID: {} already exists, throwing BookAlreadyExistsException", book.getId());
            simulateSinglePageApp(model, true, true);
            return "admin/addBook";
        }
        
        // Check if the request came from home page
        if ("home".equals(source)) {
            return "redirect:/v1/web/books/";
        }

        return "redirect:/v1/web/books/";
    }


    // Get a Single Book
    @GetMapping("/{id}")
    public String getBookById(@PathVariable(value = "id") Long bookId, Model model) throws BookNotFoundException  {
        BookWithInventoryDto book = bookInventoryService.getBookWithInventoryById(bookId);
        model.addAttribute("book", book);
        return "admin/editBook";
    }

    // Update an Existing Book
    @PutMapping("/save")
    public String updateBook(@Valid @ModelAttribute("book") BookWithInventoryDto book, BindingResult bindingResult, Model model,
            jakarta.servlet.http.HttpServletRequest request, @RequestParam(value = "source", required = false) String source)
            throws BookNotFoundException {

        logger.info("updateBook called with book ID: {}, title: '{}', author: '{}'", 
                    book.getId(), book.getTitle(), book.getAuthor());

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors found for book ID: {}, errors: {}", 
                       book.getId(), bindingResult.getFieldErrors());
            
            // Check if the request came from home page edit
            if ("home".equals(source)) {
                // If from home page, we need to return home template with both book list and edit form
                simulateSinglePageApp(model, true, false);
                
                // book object with errors is already in the model from @ModelAttribute
                return "admin/home";
            }
            
            return "admin/editBook";
        }

        if (bookInventoryService.getBookWithInventoryById(book.getId()) != null) {
            logger.info("Book with ID: {} found, proceeding with update", book.getId());
            // TODO: add customerId from security context
            bookInventoryService.updateBookInventory(book, null);
            logger.info("Book with ID: {} successfully updated", book.getId());
        } else {
            logger.error("Book with ID: {} not found, throwing BookNotFoundException", book.getId());
            throw new BookNotFoundException(book.getId());
        }

        logger.info("updateBook completed successfully for book ID: {}, redirecting to book list", book.getId());
        
        
        return "redirect:/v1/web/books/";
    }

    // Delete a Book
    @DeleteMapping("/delete/{id}")
    public String deleteBook(@PathVariable(value = "id") Long bookId, Model model) throws BookNotFoundException {
        bookInventoryService.deleteBookInventory(bookId);
        return "redirect:/v1/web/books/";
    }

    private void simulateSinglePageApp(Model model, boolean isEditMode, boolean isNewBook) {
        // If from home page, we need to return home template with both book list and add form 
        List<BookWithInventoryDto> listBooks = bookInventoryService.getAllBooksWithInventory()
            .stream()
            .toList();

        model.addAttribute("listBooks", listBooks);
        model.addAttribute("editMode", isEditMode);
        model.addAttribute("isNewBook", isNewBook);
    }

    private void simulateSinglePageApp(Model model, BookWithInventoryDto book, boolean isEditMode, boolean isNewBook) {
        model.addAttribute("book", book);
        model.addAttribute("editMode", isEditMode);
        model.addAttribute("isNewBook", isNewBook);
    }
}
