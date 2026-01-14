package ru.mentee.library.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.library.api.dto.BookInfoResponse;
import ru.mentee.library.api.dto.CreateBookRequest;
import ru.mentee.library.api.dto.OpenLibraryBookDto;
import ru.mentee.library.client.OpenLibraryClient;
import ru.mentee.library.domain.model.Book;
import ru.mentee.library.domain.repository.BookRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

  private final BookRepository bookRepository;
  private final OpenLibraryClient openLibraryClient;
  private final MeterRegistry meterRegistry;
  private Counter booksCreatedCounter;

  @PostConstruct
  public void init() {
    log.info("OpenLibraryClient initialized: {}", openLibraryClient != null);
    this.booksCreatedCounter =
        Counter.builder("books_created_total")
            .description("Total number of created books")
            .register(meterRegistry);
  }

  public BookInfoResponse getBookInfoByIsbn(String isbn) {
    log.info("Fetching book info for ISBN: {}", isbn);

    try {
      String bibkeys = "ISBN:" + isbn;
      log.debug("Calling Feign client with bibkeys: {}", bibkeys);

      Map<String, OpenLibraryBookDto> response =
          openLibraryClient.getBookByIsbn(bibkeys, "json", "data");

      OpenLibraryBookDto bookDto =
          response != null && !response.isEmpty()
              ? response.values().iterator().next() // Берём первую книгу из Map
              : null;

      log.debug("First book: {}", bookDto);

      if (bookDto == null) {
        log.warn("Book not found for ISBN: {}", isbn);
        return null;
      }

      return BookInfoResponse.from(bookDto);
    } catch (Exception e) {
      log.error("Error fetching book info for ISBN: {}", isbn, e);
      throw new RuntimeException("Failed to fetch book information", e);
    }
  }

  public Book createBookFromIsbn(String isbn) {
    BookInfoResponse bookInfo = getBookInfoByIsbn(isbn);

    if (bookInfo == null) {
      throw new IllegalArgumentException("Book not found for ISBN: " + isbn);
    }

    Book book =
        Book.builder()
            .title(bookInfo.getTitle())
            .isbn(isbn)
            .author(
                bookInfo.getAuthors() != null && !bookInfo.getAuthors().isEmpty()
                    ? bookInfo.getAuthors().get(0)
                    : "Unknown")
            .publicationYear(
                bookInfo.getPublishDate() != null ? extractYear(bookInfo.getPublishDate()) : null)
            .build();

    return bookRepository.save(book);
  }

  private Integer extractYear(String publishDate) {
    try {
      // Пытаемся извлечь год из строки (может быть "1961" или "1961-01-01")
      if (publishDate != null && !publishDate.isEmpty()) {
        String yearStr = publishDate.substring(0, 4);
        return Integer.parseInt(yearStr);
      }
    } catch (Exception e) {
      log.warn("Failed to extract year from: {}", publishDate);
    }
    return null;
  }

  public List<Book> findAll() {
    return bookRepository.findAll();
  }

  @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
  @Transactional
  public Book createBook(CreateBookRequest request) {
    Book book =
        Book.builder()
            .title(request.getTitle())
            .author(request.getAuthor())
            .publicationYear(request.getPublicationYear())
            .available(true)
            .build();
    Book savedBook = bookRepository.save(book);
    booksCreatedCounter.increment();
    return savedBook;
  }

  public Book findById(Long id) {
    return bookRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Book not found: " + id));
  }

  @Transactional
  public Book updateBook(Long id, CreateBookRequest request) {
    Book book = findById(id);
    book.setTitle(request.getTitle());
    book.setAuthor(request.getAuthor());
    book.setPublicationYear(request.getPublicationYear());
    return bookRepository.save(book);
  }

  @Transactional
  public void deleteBook(Long id) {
    bookRepository.deleteById(id);
  }
}
