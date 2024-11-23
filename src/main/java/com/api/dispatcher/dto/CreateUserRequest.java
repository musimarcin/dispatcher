package com.api.dispatcher.dto;

import com.api.dispatcher.model.Role;
import lombok.Data;

import java.util.List;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String email;
    private List<Role> roles;
}
