package com.ucd.bookshop.controllers;

import com.ucd.bookshop.controllers.dto.BookWithInventoryDto;
import com.ucd.bookshop.controllers.web.BookController;
import com.ucd.bookshop.repository.BookRepository;
import com.ucd.bookshop.service.BookInventoryService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class })
class BookControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean()
    private BookRepository bookRepository;

    @MockitoBean()
    private BookInventoryService bookInventoryService;

    @Test
    void testUpdateBookRedirects() throws Exception {
        BookWithInventoryDto bookDto = new BookWithInventoryDto();
        bookDto.setId(1L);
        bookDto.setTitle("Test Book");
        bookDto.setAuthor("Test Author");
        bookDto.setIsbn("123456789");
        bookDto.setPrice(BigDecimal.valueOf(19.99));
        bookDto.setYear(LocalDate.of(2020, 1, 1));
        bookDto.setCopies(10L);

        Mockito.when(bookInventoryService.getBookWithInventoryById(anyLong()))
                .thenReturn(bookDto);

        // Mock save behavior
        Mockito.when(bookInventoryService.updateBookInventory(any(BookWithInventoryDto.class), anyInt()))
                .thenReturn(bookDto);

        mockMvc.perform(put("/v1/web/books/save")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "1")
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("isbn", "123456789")
                .param("price", "19.99")
                .param("year", "2020-01-01")
                .param("copies", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/v1/web/books/"));
    }
}
