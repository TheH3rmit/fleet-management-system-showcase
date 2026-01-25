package com.damocles.fleet.fleetmanagementsystembackend.dto.driver;

import java.time.LocalDate;

public record UpdateDriverRequest(
        String driverLicenseNumber,
        String driverLicenseCategory,
        LocalDate driverLicenseExpiryDate
) {}
