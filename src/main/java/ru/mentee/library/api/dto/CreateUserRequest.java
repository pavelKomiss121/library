package ru.mentee.library.api.dto;

import lombok.Data;
import ru.mentee.library.security.Role;

@Data
public class CreateUserRequest {
  private String email;
  private String password;
  private Role role;
}
