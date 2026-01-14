package ru.mentee.library;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.mentee.library.api.dto.CreateBookRequest;
import ru.mentee.library.domain.model.Book;
import ru.mentee.library.domain.repository.BookRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
class LibraryIntegrationTest extends BaseIntegrationTest {

  @TestConfiguration
  @EnableWebSecurity
  @org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity(
      prePostEnabled = false)
  static class TestSecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
    }

    @Bean
    public KeyPair rsaKeyPair() {
      try {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
      } catch (Exception e) {
        throw new IllegalStateException("Failed to generate RSA key pair", e);
      }
    }

    @Bean
    public JwtDecoder jwtDecoder(KeyPair rsaKeyPair) {
      RSAPublicKey publicKey = (RSAPublicKey) rsaKeyPair.getPublic();
      return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    @Bean
    public JwtEncoder jwtEncoder(KeyPair rsaKeyPair) {
      RSAPrivateKey privateKey = (RSAPrivateKey) rsaKeyPair.getPrivate();
      RSAPublicKey publicKey = (RSAPublicKey) rsaKeyPair.getPublic();

      com.nimbusds.jose.jwk.RSAKey rsaKey =
          new com.nimbusds.jose.jwk.RSAKey.Builder(publicKey)
              .privateKey(privateKey)
              .keyID(UUID.randomUUID().toString())
              .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
              .keyUse(com.nimbusds.jose.jwk.KeyUse.SIGNATURE)
              .build();

      com.nimbusds.jose.jwk.JWKSet jwkSet = new com.nimbusds.jose.jwk.JWKSet(rsaKey);
      com.nimbusds.jose.jwk.source.ImmutableJWKSet<com.nimbusds.jose.proc.SecurityContext>
          jwkSource = new com.nimbusds.jose.jwk.source.ImmutableJWKSet<>(jwkSet);

      return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public AuthenticationManager authenticationManager(
        UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
      DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
      provider.setUserDetailsService(userDetailsService);
      provider.setPasswordEncoder(passwordEncoder);
      return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
      http.csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
          .sessionManagement(
              session ->
                  session.sessionCreationPolicy(
                      org.springframework.security.config.http.SessionCreationPolicy.STATELESS));
      return http.build();
    }
  }

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private BookRepository bookRepository;

  @DynamicPropertySource
  static void configureWireMockProperties(DynamicPropertyRegistry registry) {
    // WireMock порт будет установлен автоматически через @AutoConfigureWireMock
    // Spring Cloud Contract WireMock автоматически устанавливает свойство wiremock.server.port
    registry.add(
        "openlibrary.api.url",
        () -> "http://localhost:" + System.getProperty("wiremock.server.port", "8080"));
  }

  @Test
  void shouldCreateAndFetchBook() {
    // Given: Мокируем внешний сервис обогащения данных
    stubFor(
        get(urlMatching("/api/books\\?bibkeys=ISBN:.*&format=json&jscmd=data"))
            .willReturn(aResponse().withBodyFile("enrich-success.json")));

    // 1. Выполняем POST-запрос через API для создания книги
    CreateBookRequest request = new CreateBookRequest();
    request.setTitle("Test Book");
    request.setAuthor("Test Author");
    request.setPublicationYear(2024);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<CreateBookRequest> entity = new HttpEntity<>(request, headers);

    ResponseEntity<Book> createResponse =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/books", HttpMethod.POST, entity, Book.class);

    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
    assertNotNull(createResponse.getBody());
    Book createdBook = createResponse.getBody();
    assertNotNull(createdBook.getId());
    assertEquals("Test Book", createdBook.getTitle());
    assertEquals("Test Author", createdBook.getAuthor());
    assertEquals(2024, createdBook.getPublicationYear());

    // 2. Выполняем GET-запрос для проверки, что книга создана
    ResponseEntity<Book> getResponse =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/books/" + createdBook.getId(),
            HttpMethod.GET,
            null,
            Book.class);

    assertEquals(HttpStatus.OK, getResponse.getStatusCode());
    assertNotNull(getResponse.getBody());
    Book retrievedBook = getResponse.getBody();
    assertEquals(createdBook.getId(), retrievedBook.getId());
    assertEquals("Test Book", retrievedBook.getTitle());
    assertEquals("Test Author", retrievedBook.getAuthor());
    assertEquals(2024, retrievedBook.getPublicationYear());

    // Проверяем, что книга сохранена в БД через репозиторий
    Book dbBook = bookRepository.findById(createdBook.getId()).orElse(null);
    assertNotNull(dbBook);
    assertEquals(createdBook.getId(), dbBook.getId());
    assertEquals("Test Book", dbBook.getTitle());
    assertEquals("Test Author", dbBook.getAuthor());
    assertEquals(2024, dbBook.getPublicationYear());
  }
}
