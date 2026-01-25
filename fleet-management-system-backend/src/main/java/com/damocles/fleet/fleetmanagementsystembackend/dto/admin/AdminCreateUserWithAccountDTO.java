package com.damocles.fleet.fleetmanagementsystembackend.dto.admin;


import com.damocles.fleet.fleetmanagementsystembackend.domain.AccountStatus;
import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record AdminCreateUserWithAccountDTO(
        // USER
        @NotBlank String firstName,
        String middleName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        String phone,
        LocalDate birthDate,

        // ACCOUNT
        @NotBlank @Size(min = 3, max = 50) String login,
        @NotBlank @Size(min = 6, max = 255) String password,

        @NotNull Set<UserRole> roles,
        AccountStatus status // default (jak null -> ACTIVE)
) {}