package com.damocles.fleet.fleetmanagementsystembackend.dto.driverWorkLog;

import com.damocles.fleet.fleetmanagementsystembackend.domain.ActivityType;
import java.time.Instant;
public record DriverWorkLogDTO(
        Long id,
        Instant startTime,
        Instant endTime,
        Integer breakDuration,
        String notes,
        Long driverId,
        String driverName,
        Long transportId,
        ActivityType activityType
) {}
