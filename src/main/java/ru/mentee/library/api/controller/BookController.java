package ru.mentee.library.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.mentee.library.api.dto.BookInfoResponse;
import ru.mentee.library.api.dto.CreateBookRequest;
import ru.mentee.library.domain.model.Book;
import ru.mentee.library.service.BookService;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Book> createBook(@RequestBody CreateBookRequest request, @AuthenticationPrincipal Jwt jwt) {
        Book book = bookService.createBook(request);

        String username = jwt.getSubject();
        String scope = jwt.getClaim("scope");

        System.out.println("User: " + username);
        System.out.println("Roles: " + scope);

        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Book> updateBook(
            @PathVariable Long id,
            @RequestBody CreateBookRequest request) {
        Book book = bookService.updateBook(id, request);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/isbn/{isbn}/info")
    public ResponseEntity<BookInfoResponse> getBookInfoByIsbn(@PathVariable String isbn) {
        BookInfoResponse bookInfo = bookService.getBookInfoByIsbn(isbn);

        if (bookInfo == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(bookInfo);
    }

    @PostMapping("/isbn/{isbn}")
    public ResponseEntity<Book> createBookFromIsbn(@PathVariable String isbn) {
        Book book = bookService.createBookFromIsbn(isbn);
        return ResponseEntity.ok(book);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}



