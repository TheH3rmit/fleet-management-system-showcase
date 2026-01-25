package com.damocles.fleet.fleetmanagementsystembackend.dto.driver;

import com.damocles.fleet.fleetmanagementsystembackend.domain.DriverStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDriverStatusRequest(
        @NotNull(message = "Status is required")
        DriverStatus status
) {}
