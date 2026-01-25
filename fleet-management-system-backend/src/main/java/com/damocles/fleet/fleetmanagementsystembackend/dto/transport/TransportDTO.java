package com.damocles.fleet.fleetmanagementsystembackend.dto.transport;

import com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TransportDTO (
        Long id,
        Instant contractualDueAt,
        Instant plannedStartAt,
        Instant plannedEndAt,
        Instant actualStartAt,
        Instant actualEndAt,
        BigDecimal plannedDistanceKm,
        BigDecimal actualDistanceKm,
        Long createdById,
        Long trailerId,
        String trailerLabel,
        Long vehicleId,
        String vehicleLabel,
        TransportStatus status,
        Long pickupLocationId,
        Long deliveryLocationId,
        Long driverId
) {}
