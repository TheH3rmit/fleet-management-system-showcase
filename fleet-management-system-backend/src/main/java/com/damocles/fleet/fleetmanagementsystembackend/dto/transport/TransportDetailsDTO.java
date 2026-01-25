package com.damocles.fleet.fleetmanagementsystembackend.dto.transport;

import com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus;
import com.damocles.fleet.fleetmanagementsystembackend.dto.location.LocationDTO;

import java.math.BigDecimal;
import java.time.Instant;

public record TransportDetailsDTO(
        Long id,

        Instant contractualDueAt,
        Instant plannedStartAt,
        Instant plannedEndAt,
        Instant actualStartAt,
        Instant actualEndAt,

        BigDecimal plannedDistanceKm,
        BigDecimal actualDistanceKm,

        Long createdById,
        String createdByEmail,

        Long trailerId,
        Long vehicleId,
        Long driverId,

        TransportStatus status,

        LocationDTO pickupLocation,
        LocationDTO deliveryLocation
) {}