package com.app.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailChangeRequest(
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Email should be valid")
        String newEmail
) {}
