package com.api.dispatcher.dto;

import com.api.dispatcher.model.Role;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String email;
    private Role role;
}
