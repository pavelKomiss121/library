package ru.mentee.library.security;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import ru.mentee.library.api.dto.TokenResponse;
import ru.mentee.library.domain.model.RefreshToken;
import ru.mentee.library.domain.repository.RefreshTokenRepository;

@Service
// @RequiredArgsConstructor
@Slf4j
public class TokenService {

  private final JwtEncoder jwtEncoder;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtDecoder jwtDecoder;
  private final UserDetailsService userDetailsService;

  public TokenService(
      JwtEncoder jwtEncoder,
      RefreshTokenRepository refreshTokenRepository,
      JwtDecoder jwtDecoder,
      UserDetailsService userDetailsService) {
    this.jwtEncoder = jwtEncoder;
    this.refreshTokenRepository = refreshTokenRepository;
    this.jwtDecoder = jwtDecoder;
    this.userDetailsService = userDetailsService;
  }

  public String generateAccessToken(Authentication authentication) {
    Instant now = Instant.now();
    long expiry = 3600L;

    String scope =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));

    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expiry))
            .subject(authentication.getName())
            .claim("scope", scope)
            .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public String generateRefreshToken(Authentication authentication) {
    Instant now = Instant.now();
    long expiry = 604800L;

    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expiry))
            .subject(authentication.getName())
            .claim("type", "refresh")
            .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  @Transactional
  public void saveRefreshToken(String token, String userId) {
    RefreshToken refreshToken =
        RefreshToken.builder()
            .token(token)
            .userId(userId)
            .expiresAt(Instant.now().plusSeconds(604800L))
            .revoked(false)
            .build();

    refreshTokenRepository.save(refreshToken);
    log.debug("Saved refresh token for user: {}", userId);
  }

  @Transactional
  public void revokeRefreshToken(String token) {
    refreshTokenRepository
        .findByToken(token)
        .ifPresent(
            refreshToken -> {
              refreshToken.setRevoked(true);
              refreshTokenRepository.save(refreshToken);
              log.debug("Revoked refresh token for user: {}", refreshToken.getUserId());
            });
  }

  @Transactional
  public TokenResponse refreshAccessToken(String refreshTokenValue) {
    try {
      Jwt refreshToken = jwtDecoder.decode(refreshTokenValue);

      String type = refreshToken.getClaim("type");
      if (!type.equals("refresh")) {
        throw new JwtException("Invalid refresh token");
      }

      RefreshToken storedToken =
          refreshTokenRepository
              .findByToken(refreshTokenValue)
              .orElseThrow(() -> new JwtException("Token is not a refresh token"));

      if (storedToken.getRevoked()) {
        throw new JwtException("Token is revoked");
      }

      String username = refreshToken.getSubject();

      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      String newAccessToken = generateAccessToken(authentication);
      String newRefreshToken = generateRefreshToken(authentication);

      // Помечаем старый refresh token как отозванный
      storedToken.setRevoked(true);
      refreshTokenRepository.save(storedToken);

      // Сохраняем новый refresh token в БД
      saveRefreshToken(newRefreshToken, username);

      return new TokenResponse(newAccessToken, newRefreshToken);
    } catch (JwtException e) {
      log.error("Failed to refresh token: {}", e.getMessage());
      throw e;
    }
  }
}
