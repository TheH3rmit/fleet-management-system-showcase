package com.damocles.fleet.fleetmanagementsystembackend.dto.account;
import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record AccountRolesUpdateDTO(
        @NotEmpty(message = "roles cannot be empty")
        Set<UserRole> roles
) {}
