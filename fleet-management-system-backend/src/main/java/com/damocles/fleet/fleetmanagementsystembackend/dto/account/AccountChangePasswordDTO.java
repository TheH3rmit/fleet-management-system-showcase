package com.damocles.fleet.fleetmanagementsystembackend.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccountChangePasswordDTO(
        @Size(min = 6, message = "Old password must be at least 6 characters")
        String oldPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "New password must be at least 6 characters")
        String newPassword
) {}
