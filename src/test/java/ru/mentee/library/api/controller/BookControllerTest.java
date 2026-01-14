package ru.mentee.library.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.mentee.library.domain.model.Book;
import ru.mentee.library.service.BookService;

@WebMvcTest(
    controllers = BookController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Disabled("Не относится к заданию по мониторингу")
class BookControllerTest {

  @TestConfiguration
  static class TestConfig {
    @Bean
    public MeterRegistry meterRegistry() {
      return new SimpleMeterRegistry();
    }
  }

  @Autowired private MockMvc mockMvc;

  @MockitoBean private BookService bookService;

  @Test
  @DisplayName("Should return book by id")
  void shouldReturnBookById() throws Exception {
    // Given
    Book book =
        Book.builder()
            .id(1L)
            .title("1984")
            .author("George Orwell")
            .publicationYear(1949)
            .available(true)
            .build();

    when(bookService.findById(1L)).thenReturn(book);

    // When & Then
    mockMvc
        .perform(get("/api/books/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("1984"))
        .andExpect(jsonPath("$.author").value("George Orwell"))
        .andExpect(jsonPath("$.publicationYear").value(1949));
  }

  @Test
  @DisplayName("Should return all books")
  void shouldReturnAllBooks() throws Exception {
    // Given
    Book book1 =
        Book.builder().id(1L).title("1984").author("George Orwell").publicationYear(1949).build();

    Book book2 =
        Book.builder()
            .id(2L)
            .title("Animal Farm")
            .author("George Orwell")
            .publicationYear(1945)
            .build();

    List<Book> books = Arrays.asList(book1, book2);
    when(bookService.findAll()).thenReturn(books);

    // When & Then
    mockMvc
        .perform(get("/api/books"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].title").value("1984"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].title").value("Animal Farm"));
  }

  @Test
  @DisplayName("Should return 404 when book not found")
  void shouldReturn404WhenBookNotFound() throws Exception {
    // Given
    when(bookService.findById(999L)).thenThrow(new RuntimeException("Book not found: 999"));

    // When & Then
    mockMvc.perform(get("/api/books/999")).andExpect(status().isNotFound());
  }
}
