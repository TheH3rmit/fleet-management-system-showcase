package com.damocles.fleet.fleetmanagementsystembackend.dto.account;

import com.damocles.fleet.fleetmanagementsystembackend.domain.AccountStatus;
import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;

import java.time.Instant;
import java.util.Set;

public record AccountResponseDTO(
        Long id,
        String login,
        AccountStatus status,
        Instant createdAt,
        Instant lastLoginAt,
        Long userId,
        Set<UserRole> roles
) {}
