package ru.mentee.library.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 1000)
  private String token;

  @Column(nullable = false)
  private String userId; // email пользователя

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  @Builder.Default
  private Boolean revoked = false;

  @Column(nullable = false, updatable = false)
  @Builder.Default
  private Instant createdAt = Instant.now();
}
