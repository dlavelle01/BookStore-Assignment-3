package com.ucd.bookshop.repository;

import com.ucd.bookshop.model.Book;
import com.ucd.bookshop.model.Customer;
import com.ucd.bookshop.model.Inventory;
import com.ucd.bookshop.model.ShoppingCart;
import com.ucd.bookshop.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
class ShoppingCartRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    private Book testBook1;
    private Book testBook2;
    private Book testBook3;
    private Customer testCustomer1;
    private Customer testCustomer2;
    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        // Create users
        testUser1 = new User();
        testUser1.setUserId(UUID.randomUUID());
        testUser1.setUserName("testcustomer1");
        testUser1.setPassword("hashedpassword");
        testUser1.setSalt("salt123");
        testUser1.setRoleId(2);
        entityManager.persistAndFlush(testUser1);

        testUser2 = new User();
        testUser2.setUserId(UUID.randomUUID());
        testUser2.setUserName("testcustomer2");
        testUser2.setPassword("hashedpassword");
        testUser2.setSalt("salt456");
        testUser2.setRoleId(2);
        entityManager.persistAndFlush(testUser2);

        // Create customers
        testCustomer1 = new Customer();
        testCustomer1.setUser(testUser1);
        testCustomer1.setName("John");
        testCustomer1.setSurname("Doe");
        testCustomer1.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testCustomer1.setAddress("123 Main Street");
        testCustomer1.setPhoneNumber("1324567890");
        testCustomer1.setEmail("john.doe@test.com");
        entityManager.persistAndFlush(testCustomer1);

        testCustomer2 = new Customer();
        testCustomer2.setUser(testUser2);
        testCustomer2.setName("Jane");
        testCustomer2.setSurname("Smith");
        testCustomer2.setDateOfBirth(LocalDate.of(1985, 5, 15));
        testCustomer2.setAddress("456 John Street");
        testCustomer2.setPhoneNumber("1234567890");
        testCustomer2.setEmail("jane.smith.2@test.com");
        entityManager.persistAndFlush(testCustomer2);

        // Create test books
        testBook1 = new Book();
        testBook1.setTitle("Spring Boot Guide");
        testBook1.setAuthor("John Smith");
        testBook1.setIsbn("978-1234567890");
        testBook1.setPrice(new BigDecimal("29.99"));
        testBook1.setYear(LocalDate.of(2023, 1, 1));
        entityManager.persistAndFlush(testBook1);

        testBook2 = new Book();
        testBook2.setTitle("John P. Programming");
        testBook2.setAuthor("Oliver Doe");
        testBook2.setIsbn("978-0987654321");
        testBook2.setPrice(new BigDecimal("39.99"));
        testBook2.setYear(LocalDate.of(2023, 6, 1));
        entityManager.persistAndFlush(testBook2);

        testBook3 = new Book();
        testBook3.setTitle("Testy mc testface");
        testBook3.setAuthor("Jon Wilson");
        testBook3.setIsbn("978-123123");
        testBook3.setPrice(new BigDecimal("49.99"));
        testBook3.setYear(LocalDate.of(2023, 12, 1));
        entityManager.persistAndFlush(testBook3);

        // Create inventory for books
        Inventory inventory1 = new Inventory(testBook1, 10, null);
        entityManager.persistAndFlush(inventory1);

        Inventory inventory2 = new Inventory(testBook2, 0, null);
        entityManager.persistAndFlush(inventory2);

        Inventory inventory3 = new Inventory(testBook3, 5, null);
        entityManager.persistAndFlush(inventory3);

        entityManager.clear();
    }

    @Test
    void testFindByCustomerId() {
        // Add items to customer1's cart
        ShoppingCart cartItem1 = new ShoppingCart(testBook1, testCustomer1, false);
        ShoppingCart cartItem2 = new ShoppingCart(testBook2, testCustomer1, false);
        shoppingCartRepository.save(cartItem1);
        shoppingCartRepository.save(cartItem2);

        // Add item to customer2's cart
        ShoppingCart cartItem3 = new ShoppingCart(testBook3, testCustomer2, false);
        shoppingCartRepository.save(cartItem3);

        // Test finding by customer ID
        List<ShoppingCart> customer1Items = shoppingCartRepository.findByCustomerId(testCustomer1.getId());
        List<ShoppingCart> customer2Items = shoppingCartRepository.findByCustomerId(testCustomer2.getId());

        assertThat(customer1Items).hasSize(2);
        assertThat(customer1Items).extracting(item -> item.getBook().getTitle())
                .containsExactlyInAnyOrder("Spring Boot Guide", "John P. Programming");

        assertThat(customer2Items).hasSize(1);
        assertThat(customer2Items.get(0).getBook().getTitle()).isEqualTo("Testy mc testface");
    }
}