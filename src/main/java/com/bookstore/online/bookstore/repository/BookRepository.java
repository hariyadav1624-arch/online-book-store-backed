package com.bookstore.online.bookstore.repository;

import com.bookstore.online.bookstore.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
