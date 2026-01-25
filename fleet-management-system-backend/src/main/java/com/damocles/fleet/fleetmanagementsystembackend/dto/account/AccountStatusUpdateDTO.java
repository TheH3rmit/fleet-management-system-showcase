package com.damocles.fleet.fleetmanagementsystembackend.dto.account;


import com.damocles.fleet.fleetmanagementsystembackend.domain.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record AccountStatusUpdateDTO(
        @NotNull(message = "Status is required")
        AccountStatus status
) {}
