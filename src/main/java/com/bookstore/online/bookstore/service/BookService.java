package com.bookstore.online.bookstore.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.bookstore.online.bookstore.entity.Book;
import com.bookstore.online.bookstore.repository.BookRepository;

@Service
public class BookService {

    private final BookRepository bookRepository;

    // Constructor
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // Get all books
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Get a book by ID
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    // Add a new book
    public Book addBook(Book book) {
        return bookRepository.save(book);
    }

    // Update an existing book
    public Optional<Book> updateBook(Long id, Book updatedBook) {

        return bookRepository.findById(id).map(existingBook -> {

            existingBook.setTitle(updatedBook.getTitle());
            existingBook.setAuthor(updatedBook.getAuthor());
            existingBook.setPrice(updatedBook.getPrice());
            existingBook.setDescription(updatedBook.getDescription());
            existingBook.setStock(updatedBook.getStock());

            return bookRepository.save(existingBook);
        });
    }

    // Delete a book
    public boolean deleteBook(Long id) {

        if (!bookRepository.existsById(id)) {
            return false;
        }

        bookRepository.deleteById(id);
        return true;
    }

    // Search books by title
    public List<Book> searchBooksByTitle(String title) {
        if (title == null || title.isBlank()) {
            return List.of();
        }

        String searchTerm = title.trim().toLowerCase(Locale.ROOT);
        return bookRepository.findAll().stream()
            .filter(book -> book.getTitle() != null
                && book.getTitle().toLowerCase(Locale.ROOT).contains(searchTerm))
            .collect(Collectors.toList());
    }

    // Search books by author
    public List<Book> searchBooksByAuthor(String author) {
        if (author == null || author.isBlank()) {
            return List.of();
        }

        String searchTerm = author.trim().toLowerCase(Locale.ROOT);
        return bookRepository.findAll().stream()
            .filter(book -> book.getAuthor() != null
                && book.getAuthor().toLowerCase(Locale.ROOT).contains(searchTerm))
            .collect(Collectors.toList());
    }
}