package com.damocles.fleet.fleetmanagementsystembackend.dto.trailer;

import com.damocles.fleet.fleetmanagementsystembackend.domain.TrailerStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTrailerStatusRequest(
        @NotNull(message = "Status is required")
        TrailerStatus status
) {}
