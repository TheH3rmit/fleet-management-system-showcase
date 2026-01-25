package com.damocles.fleet.fleetmanagementsystembackend.dto.transport;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateTransportRequest(
        Instant contractualDueAt,
        @NotNull(message = "Planned start is required")
        Instant plannedStartAt,
        Instant plannedEndAt,
        @Positive(message = "Planned distance must be positive")
        BigDecimal plannedDistanceKm,
        @Positive(message = "Trailer id must be positive")
        @NotNull(message = "Trailer is required")
        Long trailerId,
        @Positive(message = "Vehicle id must be positive")
        @NotNull(message = "Vehicle is required")
        Long vehicleId,
        @Positive(message = "Pickup location id must be positive")
        @NotNull(message = "Pickup location is required")
        Long pickupLocationId,
        @Positive(message = "Delivery location id must be positive")
        @NotNull(message = "Delivery location is required")
        Long deliveryLocationId,
        @Positive(message = "Driver id must be positive")
        Long driverId
) {}
