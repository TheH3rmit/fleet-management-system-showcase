package com.damocles.fleet.fleetmanagementsystembackend.dto.account;


import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record AccountRegisterDTO(
        @NotBlank(message = "Login is required")
        @Size(min = 3, max = 50)
        String login,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be positive")
        Long userId,
        @NotEmpty(message = "Roles cannot be empty")
        Set<UserRole>roles
) {}
