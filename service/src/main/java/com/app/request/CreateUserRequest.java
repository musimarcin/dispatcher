package com.app.request;

import com.app.model.Role;
import lombok.Data;

import java.util.List;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String email;
    private List<Role> roles;
}
