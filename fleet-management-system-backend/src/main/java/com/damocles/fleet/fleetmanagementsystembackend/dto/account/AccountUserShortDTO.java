package com.damocles.fleet.fleetmanagementsystembackend.dto.account;

import com.damocles.fleet.fleetmanagementsystembackend.domain.AccountStatus;

import java.time.Instant;

public record AccountUserShortDTO(
        Long accountId,
        String login,
        AccountStatus status,
        Instant createdAt,
        Instant lastLoginAt,
        Long userId,
        String firstName,
        String lastName,
        String email
) {}
