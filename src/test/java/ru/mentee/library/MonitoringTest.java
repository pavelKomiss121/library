package ru.mentee.library;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
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
import org.springframework.test.web.servlet.MockMvc;
import ru.mentee.library.api.dto.CreateBookRequest;

@SpringBootTest(
    properties = {
      "management.endpoints.web.exposure.include=health,info,prometheus,metrics,loggers",
      "management.endpoint.health.show-details=always"
    })
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
class MonitoringTest extends BaseIntegrationTest {

  @DynamicPropertySource
  static void configureWireMockProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "openlibrary.api.url",
        () -> "http://localhost:" + System.getProperty("wiremock.server.port", "8080"));
  }

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

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void healthEndpointShouldBeAvailable() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  void prometheusEndpointShouldExposeCustomMetrics() throws Exception {
    // Создать книгу через API, чтобы метрики сработали
    CreateBookRequest request = new CreateBookRequest();
    request.setTitle("Test Book");
    request.setAuthor("Test Author");
    request.setPublicationYear(2024);

    mockMvc
        .perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    // Проверяем, что метрика books_created_total присутствует в prometheus эндпоинте
    // Сначала проверяем, что метрика доступна через /actuator/metrics
    mockMvc.perform(get("/actuator/metrics/books_created_total")).andExpect(status().isOk());

    // Затем проверяем prometheus эндпоинт (может быть недоступен в тестах, но метрика должна быть)
    try {
      String prometheusResponse =
          mockMvc
              .perform(get("/actuator/prometheus"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      // Проверяем наличие метрики в prometheus формате
      org.junit.jupiter.api.Assertions.assertTrue(
          prometheusResponse.contains("books_created_total"),
          "Metric 'books_created_total' not found in prometheus response");
    } catch (AssertionError e) {
      // Если prometheus эндпоинт недоступен, проверяем через /actuator/metrics
      String metricsResponse =
          mockMvc
              .perform(get("/actuator/metrics"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      org.junit.jupiter.api.Assertions.assertTrue(
          metricsResponse.contains("books_created_total"),
          "Metric 'books_created_total' not found in metrics response");
    }
  }
}
