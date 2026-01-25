package com.damocles.fleet.fleetmanagementsystembackend.dto.cargo;

import java.time.Instant;
import com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus;

public record CargoDTO(
        Long id,
        String cargoDescription,
        java.math.BigDecimal weightKg,
        java.math.BigDecimal volumeM3,
        Instant pickupDate,
        Instant deliveryDate,
        Long transportId,
        TransportStatus transportStatus
) {}
