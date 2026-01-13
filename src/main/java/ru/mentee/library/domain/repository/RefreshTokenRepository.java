package ru.mentee.library.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mentee.library.domain.model.RefreshToken;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String refreshToken);

  void deleteByUserId(String userId);

  void deleteByExpiresAtBefore(Instant now);
}
