package com.api.dispatcher.dto;

import com.api.dispatcher.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private String email;
    private Instant registered;
    private List<Role> roles;
}
