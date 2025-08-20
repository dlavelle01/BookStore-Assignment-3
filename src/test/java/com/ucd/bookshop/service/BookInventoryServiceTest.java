package com.ucd.bookshop.service;

import com.ucd.bookshop.controllers.dto.BookWithInventoryDto;
import com.ucd.bookshop.exception.BookAlreadyExistsException;
import com.ucd.bookshop.exception.BookNotFoundException;

import com.ucd.bookshop.model.Inventory;
import com.ucd.bookshop.repository.BookRepository;
import com.ucd.bookshop.repository.InventoryRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=password",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.defer-datasource-initialization=false",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.sql.init.mode=never"
})
@Transactional
class BookInventoryServiceTest {

    @Autowired
    private BookInventoryService bookInventoryService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void testCreateBookInventory() {
        BookWithInventoryDto bookWithInventoryDto = new BookWithInventoryDto();
        bookWithInventoryDto.setTitle("Test Book");
        bookWithInventoryDto.setAuthor("Test Author");
        bookWithInventoryDto.setIsbn("1234567890");
        bookWithInventoryDto.setPrice(BigDecimal.valueOf(10.00));
        bookWithInventoryDto.setYear(LocalDate.of(2020, 1, 1));
        bookWithInventoryDto.setCopies(10L);

        BookWithInventoryDto result = null;
        try {
            result = bookInventoryService.createBookInventory(bookWithInventoryDto, 1234567890);
        } catch (BookAlreadyExistsException e) {
            fail("BookAlreadyExistsException should not be thrown");
        }

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Book");
        assertThat(result.getAuthor()).isEqualTo("Test Author");
        assertThat(result.getIsbn()).isEqualTo("1234567890");
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(10.00));
    }   

    @Test
    void testUpdateBookInventoryNegative() {
        BookWithInventoryDto bookWithInventoryDto = new BookWithInventoryDto();
        bookWithInventoryDto.setTitle("Test Book");
        bookWithInventoryDto.setAuthor("Test Author");
        bookWithInventoryDto.setIsbn("1234567890");
        bookWithInventoryDto.setPrice(BigDecimal.valueOf(10.00));
        bookWithInventoryDto.setYear(LocalDate.of(2020, 1, 1));
        bookWithInventoryDto.setCopies(10L);


        BookWithInventoryDto newBookWithInventoryDto = null;
        try {
            newBookWithInventoryDto = bookInventoryService.createBookInventory(bookWithInventoryDto, 1234567890);
        } catch (BookAlreadyExistsException e) {
            fail("BookAlreadyExistsException should not be thrown");
        }

        BookWithInventoryDto resultUpdate = null;
        try {
            newBookWithInventoryDto.setCopies(4L);
            resultUpdate = bookInventoryService.updateBookInventory(newBookWithInventoryDto, 1234567890);
        } catch (BookNotFoundException e) {
            fail("BookNotFoundException should not be thrown");
        }

        BookWithInventoryDto resultPersisted = null;
        try {
             resultPersisted = bookInventoryService.getBookWithInventoryById(newBookWithInventoryDto.getId());
        } catch (BookNotFoundException e) {
            fail("BookNotFoundException should not be thrown");
        }

        List<Inventory> inventories = inventoryRepository.findAllByBookId(newBookWithInventoryDto.getId());
        
        // 10 initially persisted. 4 are requested to be set. 6 are removed from the inventory
        assertThat(resultUpdate).isNotNull();
        assertThat(resultUpdate.getCopies()).isEqualTo(4L);
        assertThat(resultPersisted).isNotNull();
        assertThat(resultPersisted.getCopies()).isEqualTo(4L);
        assertThat(inventories).hasSize(2);
        assertThat(inventories.get(1).getCopies()).isEqualTo(-6);   
    }

    @Test
    void testUpdateBookInventoryPositive() {
        BookWithInventoryDto bookWithInventoryDto = new BookWithInventoryDto();
        bookWithInventoryDto.setTitle("Test Book");
        bookWithInventoryDto.setAuthor("Test Author");
        bookWithInventoryDto.setIsbn("1234567890");
        bookWithInventoryDto.setPrice(BigDecimal.valueOf(10.00));
        bookWithInventoryDto.setYear(LocalDate.of(2020, 1, 1));
        bookWithInventoryDto.setCopies(10L);

        BookWithInventoryDto newBookWithInventoryDto = null;
        try {
            newBookWithInventoryDto = bookInventoryService.createBookInventory(bookWithInventoryDto, 2);
        } catch (BookAlreadyExistsException e) {
            fail("BookAlreadyExistsException should not be thrown");
        }

        BookWithInventoryDto resultUpdate = null;
        try {
            newBookWithInventoryDto.setCopies(14L);
            resultUpdate = bookInventoryService.updateBookInventory(newBookWithInventoryDto, 2);
        } catch (BookNotFoundException e) {
            fail("BookNotFoundException should not be thrown");
        }

        BookWithInventoryDto resultPersisted = null;
        try {
            resultPersisted = bookInventoryService.getBookWithInventoryById(newBookWithInventoryDto.getId());
        } catch (BookNotFoundException e) {
            fail("BookNotFoundException should not be thrown");
        }

        List<Inventory> inventories = inventoryRepository.findAllByBookId(newBookWithInventoryDto.getId());

        assertThat(resultUpdate).isNotNull();
        assertThat(resultUpdate.getCopies()).isEqualTo(14L);
        assertThat(resultPersisted).isNotNull();
        assertThat(resultPersisted.getCopies()).isEqualTo(14L);
        assertThat(inventories).hasSize(2);
        assertThat(inventories.get(0).getCopies()).isEqualTo(10);    
        assertThat(inventories.get(1).getCopies()).isEqualTo(4);   
    }
}