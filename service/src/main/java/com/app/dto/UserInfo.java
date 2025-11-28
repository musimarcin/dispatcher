package com.app.dto;

import com.app.model.Role;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class UserInfo {

    private Long id;
    private String username;
    private Set<String> roles;

    public UserInfo(Long id, String username, Set<Role> roles) {
        this.id = id;
        this.username = username;
        this.roles = roles.stream().map(String::valueOf).map(role -> role.substring(5)).collect(Collectors.toSet());
    }
}
