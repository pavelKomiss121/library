package ru.mentee.library.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.mentee.library.domain.model.User;
import ru.mentee.library.domain.repository.UserRepository;
import ru.mentee.library.security.Role;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {
    // Создаём тестовых пользователей, если их ещё нет
    if (userRepository.findByEmail("user@example.com").isEmpty()) {
      User user = User.builder()
          .email("user@example.com")
          .password(passwordEncoder.encode("password"))
          .role(Role.USER)
          .active(true)
          .build();
      userRepository.save(user);
    }

    if (userRepository.findByEmail("librarian@example.com").isEmpty()) {
      User librarian = User.builder()
          .email("librarian@example.com")
          .password(passwordEncoder.encode("password"))
          .role(Role.LIBRARIAN)
          .active(true)
          .build();
      userRepository.save(librarian);
    }

    if (userRepository.findByEmail("admin@example.com").isEmpty()) {
      User admin = User.builder()
          .email("admin@example.com")
          .password(passwordEncoder.encode("password"))
          .role(Role.ADMIN)
          .active(true)
          .build();
      userRepository.save(admin);
    }
  }
}