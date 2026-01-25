package com.damocles.fleet.fleetmanagementsystembackend.dto.driver;

import com.damocles.fleet.fleetmanagementsystembackend.domain.DriverStatus;

import java.time.LocalDate;

public record  DriverDTO(
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String driverLicenseNumber,
        String driverLicenseCategory,
        LocalDate driverLicenseExpiryDate,
        DriverStatus driverStatus,
        boolean hasTransports,
        boolean hasWorkLogs
) {}
