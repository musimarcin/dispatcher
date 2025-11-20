package com.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RoleChangeRequest(
        @NotNull(message = "Roles cannot be empty")
        @Size(min = 1, message = "Select at least one role")
        Set<String> roles
) {}