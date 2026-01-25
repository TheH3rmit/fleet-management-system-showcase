package com.damocles.fleet.fleetmanagementsystembackend.dto.trailer;

import com.damocles.fleet.fleetmanagementsystembackend.domain.TrailerStatus;

import java.math.BigDecimal;

public record TrailerDTO(
        Long id,
        String name,
        String licensePlate,
        BigDecimal payload,
        BigDecimal volume,
        TrailerStatus trailerStatus,
        boolean assignedToTransport,
        boolean inProgressAssigned
) {}
