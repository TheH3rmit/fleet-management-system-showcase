package com.damocles.fleet.fleetmanagementsystembackend.dto.vehicle;

import com.damocles.fleet.fleetmanagementsystembackend.domain.VehicleStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateVehicleStatusRequest(
        @NotNull(message = "Status is required")
        VehicleStatus status
) {}
