package com.damocles.fleet.fleetmanagementsystembackend.dto.driverWorkLog;

import com.damocles.fleet.fleetmanagementsystembackend.domain.ActivityType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateDriverWorkLogRequest(
        @NotNull(message = "Start time is required")
        Instant startTime,
        Instant endTime,
        @PositiveOrZero(message = "Break duration must be >= 0")
        Integer breakDuration,
        @Size(max = 500, message = "Notes must be <= 500 characters")
        String notes,
        @NotNull(message = "Driver is required")
        @Positive(message = "Driver id must be positive")
        Long driverId,
        @NotNull(message = "Transport is required")
        @Positive(message = "Transport id must be positive")
        Long transportId,
        @NotNull(message = "Activity type is required")
        ActivityType activityType
) {}
