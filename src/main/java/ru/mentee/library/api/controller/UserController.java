package ru.mentee.library.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mentee.library.api.dto.CreateUserRequest;
import ru.mentee.library.domain.model.User;
import ru.mentee.library.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  @PostMapping("/register")
  public ResponseEntity<User> register(@RequestBody CreateUserRequest request) {
    User user = userService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }

}
