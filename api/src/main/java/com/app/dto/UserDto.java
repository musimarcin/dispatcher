package com.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank(message = "Username cannot be empty")
    private String username;
    @NotBlank(message = "Password cannot be empty")
    private String password;
    @Email
    @NotBlank(message = "E-mail cannot be empty")
    private String email;
    private Instant registered;
    @NotNull(message = "Roles cannot be empty")
    @Size(min = 1, message = "Select at least one role")
    private Set<String> roles;
}
