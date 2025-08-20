package com.ucd.bookshop.controllers.web;

import com.ucd.bookshop.service.BookInventoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.ucd.bookshop.controllers.dto.BookWithInventoryDto;

import java.util.List;


@Controller
@RequestMapping("/v1/web")
public class HomeController {
    
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final BookInventoryService bookInventoryService;

    @Autowired
    public HomeController(BookInventoryService bookInventoryService) {
        this.bookInventoryService = bookInventoryService;
    }

    @GetMapping("/home")
    public String showHome(Model model) {
        logger.info("Showing home page");

        // Fetch books data and add to model for the fragment
        List<BookWithInventoryDto> listBooks = bookInventoryService.getAllBooksWithInventory()
        .stream()
        .toList();
        
        model.addAttribute("listBooks", listBooks);
        return "home";
    }

    @GetMapping("/access-denied")
    public String showAccessDeniedPage(Model model) {
        model.addAttribute("errorMessage", "Access Denied: You don't have permission to access this resource.");
        return "error/access-denied";
    }
}
