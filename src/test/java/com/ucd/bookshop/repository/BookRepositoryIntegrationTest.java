package com.ucd.bookshop.repository;

import com.ucd.bookshop.model.Book;
import com.ucd.bookshop.model.BookInventory;
import com.ucd.bookshop.model.Inventory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) 
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
class BookRepositoryIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void testSaveAndFind() {
        Book book = new Book();
        book.setTitle("Spring Boot Guide");
        book.setIsbn("123-456-789");
        book.setAuthor("John Doe");
        book.setYear(LocalDate.of(2020, 1, 1));
        book.setPrice(new BigDecimal("19.99"));

        bookRepository.save(book);

        List<Book> books = bookRepository.findAll();
        assertThat(books).hasSize(1);
        Book saved = books.get(0);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Spring Boot Guide");
        assertThat(saved.getLastModifiedDate()).isNotNull();
    }

    @Test
    void testFindAllBooksWithInventory() {
        Book book = new Book();
        book.setTitle("Spring Boot Guide");
        book.setIsbn("123-456-789");
        book.setAuthor("John Doe");
        book.setYear(LocalDate.of(2020, 1, 1));
        book.setPrice(new BigDecimal("19.99"));

        bookRepository.save(book);

        Inventory inventory = new Inventory();
        inventory.setBook(book);
        inventory.setCopies(10);
        inventoryRepository.save(inventory);

        List<BookInventory> books = bookRepository.findAllBooksWithInventory();
        assertThat(books).hasSize(1);
        BookInventory saved = books.get(0);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Spring Boot Guide");
        assertThat(saved.getYear()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(saved.getPrice()).isEqualTo(new BigDecimal("19.99"));
        assertThat(saved.getCopies()).isEqualTo(10);
    }
}

