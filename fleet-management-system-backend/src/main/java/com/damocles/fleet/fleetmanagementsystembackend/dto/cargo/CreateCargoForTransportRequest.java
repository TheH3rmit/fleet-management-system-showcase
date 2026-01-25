package com.damocles.fleet.fleetmanagementsystembackend.dto.cargo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateCargoForTransportRequest(
        String cargoDescription,
        @NotNull(message = "Weight is required")
        @Positive(message = "Weight must be positive")
        BigDecimal weightKg,
        @NotNull(message = "Volume is required")
        @Positive(message = "Volume must be positive")
        BigDecimal volumeM3,
        Instant pickupDate,
        Instant deliveryDate
) {}
