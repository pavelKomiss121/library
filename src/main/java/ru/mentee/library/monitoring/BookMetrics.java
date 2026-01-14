package ru.mentee.library.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.mentee.library.domain.repository.BookRepository;

@Component
@RequiredArgsConstructor
public class BookMetrics {

  private final BookRepository bookRepository;
  private final MeterRegistry meterRegistry;

  @PostConstruct
  public void registerMetrics() {
    Gauge.builder("books_total", bookRepository, BookRepository::count)
        .description("Total number of books in the library")
        .register(meterRegistry);
  }
}
