package ru.mentee.library.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.mentee.library.domain.model.Book;

@DataJpaTest
class BookRepositoryTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private BookRepository bookRepository;

  @Test
  @DisplayName("Should find book by id")
  void shouldFindBookById() {
    // Given
    Book book =
        Book.builder()
            .title("Test Book")
            .author("Test Author")
            .publicationYear(2024)
            .isbn("12345")
            .available(true)
            .build();

    Book savedBook = entityManager.persistAndFlush(book);

    // When
    Optional<Book> found = bookRepository.findById(savedBook.getId());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getTitle()).isEqualTo("Test Book");
    assertThat(found.get().getAuthor()).isEqualTo("Test Author");
    assertThat(found.get().getIsbn()).isEqualTo("12345");
  }

  @Test
  @DisplayName("Should save book")
  void shouldSaveBook() {
    // Given
    Book book =
        Book.builder()
            .title("New Book")
            .author("New Author")
            .publicationYear(2024)
            .isbn("67890")
            .available(true)
            .build();

    // When
    Book savedBook = bookRepository.save(book);

    // Then
    assertThat(savedBook.getId()).isNotNull();
    assertThat(savedBook.getTitle()).isEqualTo("New Book");
    assertThat(bookRepository.findById(savedBook.getId())).isPresent();
  }

  @Test
  @DisplayName("Should delete book")
  void shouldDeleteBook() {
    // Given
    Book book =
        Book.builder()
            .title("Book to Delete")
            .author("Author")
            .publicationYear(2024)
            .isbn("11111")
            .available(true)
            .build();

    Book savedBook = entityManager.persistAndFlush(book);

    // When
    bookRepository.deleteById(savedBook.getId());

    // Then
    assertThat(bookRepository.findById(savedBook.getId())).isEmpty();
  }

  @Test
  @DisplayName("Should find all books")
  void shouldFindAllBooks() {
    // Given
    Book book1 =
        Book.builder()
            .title("Book 1")
            .author("Author 1")
            .publicationYear(2024)
            .isbn("11111")
            .available(true)
            .build();

    Book book2 =
        Book.builder()
            .title("Book 2")
            .author("Author 2")
            .publicationYear(2024)
            .isbn("22222")
            .available(true)
            .build();

    entityManager.persistAndFlush(book1);
    entityManager.persistAndFlush(book2);

    // When
    var allBooks = bookRepository.findAll();

    // Then
    assertThat(allBooks).hasSizeGreaterThanOrEqualTo(2);
    assertThat(allBooks).extracting(Book::getTitle).contains("Book 1", "Book 2");
  }
}
