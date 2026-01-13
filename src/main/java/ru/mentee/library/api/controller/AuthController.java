package ru.mentee.library.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mentee.library.api.dto.LoginRequest;
import ru.mentee.library.api.dto.RefreshTokenRequest;
import ru.mentee.library.api.dto.TokenResponse;
import ru.mentee.library.security.TokenService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
  private final AuthenticationManager authenticationManager;
  private final TokenService tokenService;

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
    log.info("Login attempt for user: {}", request.username());

    try{
      Authentication authentication = new UsernamePasswordAuthenticationToken(
          request.username(),
          request.password()
      );

      Authentication authenticated = authenticationManager.authenticate(authentication);

      String accessToken = tokenService.generateAccessToken(authenticated);
      String refreshToken = tokenService.generateRefreshToken(authenticated);

      // Сохраняем refresh token в БД
      tokenService.saveRefreshToken(refreshToken, authenticated.getName());

      log.info("User {} successfully logged in", request.username());

      return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
    } catch (Exception e) {
      log.warn("Login failed for user: {}", request.username(), e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
    log.info("Refresh token request");

    try{
      TokenResponse tokens = tokenService.refreshAccessToken(request.refreshToken());
      return ResponseEntity.ok(tokens);
    }catch (Exception e) {
      log.warn("Invalid refresh token: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
    log.info("Logout request");
    tokenService.revokeRefreshToken(request.refreshToken());
    return ResponseEntity.ok().build();
  }


}
