package ru.mentee.library.monitoring;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import ru.mentee.library.domain.repository.BookRepository;

@Component
@RequiredArgsConstructor
public class LibraryHealthIndicator implements HealthIndicator {

  private final BookRepository bookRepository;

  @Override
  public Health health() {
    try {
      long bookCount = bookRepository.count();
      return Health.up()
          .withDetail("library", "operational")
          .withDetail("totalBooks", bookCount)
          .build();
    } catch (Exception e) {
      return Health.down()
          .withDetail("library", "unavailable")
          .withDetail("error", e.getMessage())
          .build();
    }
  }
}
