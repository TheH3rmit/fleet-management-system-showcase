package com.damocles.fleet.fleetmanagementsystembackend.dto.transport;
import com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTransportStatusRequest(
        @NotNull(message = "Status is required")
        TransportStatus status
) {}
