package com.damocles.fleet.fleetmanagementsystembackend.dto.user;


import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;
import java.util.Set;

public record UserUpdateDTO(
        String firstName,
        String middleName,
        String lastName,
        @Email String email,
        String phone,
        LocalDate birthDate
) {}