package com.damocles.fleet.fleetmanagementsystembackend.dto.vehicle;

import com.damocles.fleet.fleetmanagementsystembackend.domain.VehicleStatus;

import java.time.LocalDate;

public record VehicleDTO(
        Long id,
        String manufacturer,
        String model,
        LocalDate dateOfProduction,
        Integer mileage,
        String fuelType,
        VehicleStatus vehicleStatus,
        String licensePlate,
        Integer allowedLoad,
        String insuranceNumber,
        boolean assignedToTransport,
        boolean inProgressAssigned
) {}
