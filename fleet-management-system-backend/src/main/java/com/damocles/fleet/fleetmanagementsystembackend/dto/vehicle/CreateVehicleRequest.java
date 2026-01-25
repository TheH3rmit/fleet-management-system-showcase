package com.damocles.fleet.fleetmanagementsystembackend.dto.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public record CreateVehicleRequest(
        @NotBlank(message = "Manufacturer is required")
        String manufacturer,
        @NotBlank(message = "Model is required")
        String model,
        LocalDate dateOfProduction,
        @PositiveOrZero(message = "Mileage must be >= 0")
        Integer mileage,
        String fuelType,
        @NotBlank(message = "License plate is required")
        String licensePlate,
        @PositiveOrZero(message = "Allowed load must be >= 0")
        Integer allowedLoad,
        String insuranceNumber
) {}
