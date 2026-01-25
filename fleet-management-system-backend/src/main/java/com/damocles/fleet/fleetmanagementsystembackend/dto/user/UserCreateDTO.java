package com.damocles.fleet.fleetmanagementsystembackend.dto.user;

import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.Set;

public record UserCreateDTO(
        @NotBlank String firstName,
        String middleName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        String phone,
        LocalDate birthDate
) {}