package com.damocles.fleet.fleetmanagementsystembackend.dto.user;

import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;

import java.time.LocalDate;
import java.util.Set;

public record UserResponseDTO(
        Long id,
        String firstName,
        String middleName,
        String lastName,
        String email,
        String phone,
        LocalDate birthDate,
        Long accountId
) {}