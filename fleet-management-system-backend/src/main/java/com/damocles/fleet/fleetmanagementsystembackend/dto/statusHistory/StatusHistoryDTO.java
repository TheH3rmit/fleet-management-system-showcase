package com.damocles.fleet.fleetmanagementsystembackend.dto.statusHistory;


import com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus;

import java.time.Instant;

public record StatusHistoryDTO(
        Long id,
        Long transportId,
        TransportStatus status,
        Instant changedAt,
        Long changedById,
        String changedByName
) {}