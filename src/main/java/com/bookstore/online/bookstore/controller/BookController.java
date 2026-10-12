package com.bookstore.online.bookstore.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookstore.online.bookstore.entity.Book;
import com.bookstore.online.bookstore.service.BookService;

import jakarta.validation.Valid;
@CrossOrigin(origins = {
    "http://127.0.0.1:5500",
    "http://localhost:5500"
})
@RestController
@RequestMapping("/api/books")

public class BookController {

    private final BookService bookService;

    // Constructor
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // 1. Get all books
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    // 2. Get a book by ID
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3. Add a new book
    @PostMapping
    public ResponseEntity<Book> addBook(
            @Valid @RequestBody Book book) {

        Book savedBook = bookService.addBook(book);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedBook);
    }

    // 4. Update an existing book
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody Book book) {

        return bookService.updateBook(id, book)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 5. Delete a book
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {

        boolean deleted = bookService.deleteBook(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    // 6. Search books by title
    @GetMapping("/search/title")
    public ResponseEntity<List<Book>> searchByTitle(
            @RequestParam String title) {

        return ResponseEntity.ok(
                bookService.searchBooksByTitle(title)
        );
    }

    // 7. Search books by author
    @GetMapping("/search/author")
    public ResponseEntity<List<Book>> searchByAuthor(
            @RequestParam String author) {

        return ResponseEntity.ok(
                bookService.searchBooksByAuthor(author)
        );
    }

    // 8. Get books with pagination and sorting
    @GetMapping("/page")
    public ResponseEntity<Page<Book>> getBooksPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        // Validate page number and page size
        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest().build();
        }

        // Allow only valid sorting fields
        List<String> allowedFields = List.of(
                "id", "title", "author", "price", "stock"
        );

        if (!allowedFields.contains(sortBy)) {
            return ResponseEntity.badRequest().build();
        }

        // Determine sorting direction
        Sort sort;

        if (direction.equalsIgnoreCase("desc")) {
            sort = Sort.by(sortBy).descending();
        } else if (direction.equalsIgnoreCase("asc")) {
            sort = Sort.by(sortBy).ascending();
        } else {
            return ResponseEntity.badRequest().build();
        }

        // Create pagination request
        Pageable pageable = PageRequest.of(page, size, sort);

        // Fetch paginated books
        List<Book> allBooks = new ArrayList<>(bookService.getAllBooks());
        Comparator<Book> comparator = (left, right) -> {
            Object leftValue = new BeanWrapperImpl(left).getPropertyValue(sortBy);
            Object rightValue = new BeanWrapperImpl(right).getPropertyValue(sortBy);

            if (leftValue == null) {
                return rightValue == null ? 0 : -1;
            }
            if (rightValue == null) {
                return 1;
            }
            return comparePropertyValues(leftValue, rightValue);
        };

        if (direction.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }
        allBooks.sort(comparator);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allBooks.size());
        List<Book> content = start >= allBooks.size()
                ? List.of()
                : allBooks.subList(start, end);
        Page<Book> books = new PageImpl<>(content, pageable, allBooks.size());

        return ResponseEntity.ok(books);
    }

    @SuppressWarnings("unchecked")
    private static int comparePropertyValues(Object leftValue, Object rightValue) {
        return ((Comparable<Object>) leftValue).compareTo(rightValue);
    }
}