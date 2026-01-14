package ru.mentee.library.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mentee.library.api.dto.CreateBookRequest;
import ru.mentee.library.client.OpenLibraryClient;
import ru.mentee.library.domain.model.Book;
import ru.mentee.library.domain.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

  @Mock private BookRepository bookRepository;

  @Mock private OpenLibraryClient openLibraryClient;

  @InjectMocks private BookService bookService;

  @Test
  @DisplayName("Should return book when book exists")
  void whenBookExists_findById_returnsBook() {
    // Given
    Book book =
        Book.builder()
            .id(1L)
            .title("1984")
            .author("George Orwell")
            .publicationYear(1949)
            .available(true)
            .build();

    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

    // When
    Book result = bookService.findById(1L);

    // Then
    assertThat(result.getTitle()).isEqualTo("1984");
    assertThat(result.getAuthor()).isEqualTo("George Orwell");
    verify(bookRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Should throw exception when book not found")
  void whenBookNotExists_findById_throwsException() {
    // Given
    when(bookRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> bookService.findById(999L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Book not found: 999");
    verify(bookRepository, times(1)).findById(999L);
  }

  @Test
  @DisplayName("Should return all books")
  void shouldReturnAllBooks() {
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
    when(bookRepository.findAll()).thenReturn(books);

    // When
    List<Book> result = bookService.findAll();

    // Then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getTitle()).isEqualTo("1984");
    assertThat(result.get(1).getTitle()).isEqualTo("Animal Farm");
    verify(bookRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Should create book")
  void shouldCreateBook() {
    // Given
    CreateBookRequest request = new CreateBookRequest();
    request.setTitle("New Book");
    request.setAuthor("New Author");
    request.setPublicationYear(2024);

    Book savedBook =
        Book.builder()
            .id(1L)
            .title("New Book")
            .author("New Author")
            .publicationYear(2024)
            .available(true)
            .build();

    when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

    // When
    Book result = bookService.createBook(request);

    // Then
    assertThat(result.getTitle()).isEqualTo("New Book");
    assertThat(result.getAuthor()).isEqualTo("New Author");
    assertThat(result.getPublicationYear()).isEqualTo(2024);
    assertThat(result.getAvailable()).isTrue();
    verify(bookRepository, times(1)).save(any(Book.class));
  }

  @Test
  @DisplayName("Should update book")
  void shouldUpdateBook() {
    // Given
    Book existingBook =
        Book.builder()
            .id(1L)
            .title("Old Title")
            .author("Old Author")
            .publicationYear(2020)
            .available(true)
            .build();

    CreateBookRequest request = new CreateBookRequest();
    request.setTitle("Updated Title");
    request.setAuthor("Updated Author");
    request.setPublicationYear(2024);

    when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
    when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

    // When
    Book result = bookService.updateBook(1L, request);

    // Then
    assertThat(result.getTitle()).isEqualTo("Updated Title");
    assertThat(result.getAuthor()).isEqualTo("Updated Author");
    assertThat(result.getPublicationYear()).isEqualTo(2024);
    verify(bookRepository, times(1)).findById(1L);
    verify(bookRepository, times(1)).save(any(Book.class));
  }

  @Test
  @DisplayName("Should delete book")
  void shouldDeleteBook() {
    // Given
    doNothing().when(bookRepository).deleteById(1L);

    // When
    bookService.deleteBook(1L);

    // Then
    verify(bookRepository, times(1)).deleteById(1L);
  }
}
