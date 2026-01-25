package com.damocles.fleet.fleetmanagementsystembackend.dto.driver;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateDriverRequest(
        @NotNull(message = "User is required")
        @Positive(message = "User id must be positive")
        Long userId,
        String driverLicenseNumber,
        String driverLicenseCategory,
        LocalDate driverLicenseExpiryDate
) {}
