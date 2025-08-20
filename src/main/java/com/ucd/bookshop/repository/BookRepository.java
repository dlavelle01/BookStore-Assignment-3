package com.ucd.bookshop.repository;

import com.ucd.bookshop.model.Book;
import com.ucd.bookshop.model.BookInventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

   
    /** Join on the classes(tables) and group by the id */
    @Query("""
        select new com.ucd.bookshop.model.BookInventory(b.id, b.title, b.isbn, b.author, b.price, b.year, COALESCE(SUM(i.copies), 0))
        from Book b 
        left join Inventory i on i.book.id = b.id 
        group by b.id
        order by b.id desc
        """)
    List<BookInventory> findAllBooksWithInventory();

    
    @Query("""
        select new com.ucd.bookshop.model.BookInventory(b.id, b.title, b.isbn, b.author, b.price, b.year, COALESCE(SUM(i.copies), 0))
        from Book b 
        left join Inventory i on i.book.id = b.id 
        where b.id = :bookId 
        group by b.id
        """)
    BookInventory findBookWithInventoryById(Long bookId);
}