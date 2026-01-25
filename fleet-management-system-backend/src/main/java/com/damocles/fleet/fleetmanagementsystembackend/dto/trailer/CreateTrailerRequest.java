package com.damocles.fleet.fleetmanagementsystembackend.dto.trailer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateTrailerRequest(
        String name,
        String licensePlate,
        @NotNull(message = "Payload is required")
        @Positive(message = "Payload must be positive")
        BigDecimal payload,
        @NotNull(message = "Volume is required")
        @Positive(message = "Volume must be positive")
        BigDecimal volume
) {}
