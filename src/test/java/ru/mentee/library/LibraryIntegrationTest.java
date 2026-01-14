package ru.mentee.library;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.mentee.library.domain.model.Book;
import ru.mentee.library.domain.repository.BookRepository;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class LibraryIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

  @DynamicPropertySource
  static void setDatasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    registry.add(
        "spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
  }

  @Autowired private BookRepository bookRepository;

  @Test
  void shouldSaveAndRetrieveBook() {
    // Создаем книгу
    Book book =
        Book.builder()
            .title("Test Book")
            .author("Test Author")
            .publicationYear(2024)
            .isbn("978-0-123456-78-9")
            .available(true)
            .build();

    // Сохраняем книгу
    Book savedBook = bookRepository.save(book);
    assertNotNull(savedBook.getId());
    assertEquals("Test Book", savedBook.getTitle());
    assertEquals("Test Author", savedBook.getAuthor());
    assertEquals(2024, savedBook.getPublicationYear());

    // Получаем книгу из БД
    Book retrievedBook = bookRepository.findById(savedBook.getId()).orElse(null);
    assertNotNull(retrievedBook);
    assertEquals(savedBook.getId(), retrievedBook.getId());
    assertEquals("Test Book", retrievedBook.getTitle());
    assertEquals("Test Author", retrievedBook.getAuthor());
    assertEquals(2024, retrievedBook.getPublicationYear());
  }
}
