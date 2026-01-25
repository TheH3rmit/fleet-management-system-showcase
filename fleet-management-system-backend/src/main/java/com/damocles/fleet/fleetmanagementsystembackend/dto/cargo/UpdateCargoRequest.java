package com.damocles.fleet.fleetmanagementsystembackend.dto.cargo;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdateCargoRequest(
        String cargoDescription,
        @Positive(message = "Weight must be positive")
        BigDecimal weightKg,
        @Positive(message = "Volume must be positive")
        BigDecimal volumeM3,
        Instant pickupDate,
        Instant deliveryDate
) {}
