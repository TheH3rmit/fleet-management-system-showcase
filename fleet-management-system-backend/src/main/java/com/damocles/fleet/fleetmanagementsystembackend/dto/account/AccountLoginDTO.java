package com.damocles.fleet.fleetmanagementsystembackend.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record AccountLoginDTO(
        @NotBlank(message = "Login is required")
        @Size(min = 1, max = 50)
        String login,

        @NotBlank(message = "Password is required")
        @Size(min = 1, message = "Password must be at least 1 characters")
        String password
) {}