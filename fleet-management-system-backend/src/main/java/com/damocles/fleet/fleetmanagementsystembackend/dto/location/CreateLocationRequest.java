package com.damocles.fleet.fleetmanagementsystembackend.dto.location;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CreateLocationRequest(
        @NotBlank(message = "Street is required")
        String street,
        @NotBlank(message = "Building number is required")
        String buildingNumber,
        @NotBlank(message = "City is required")
        String city,
        @NotBlank(message = "Postcode is required")
        String postcode,
        @NotBlank(message = "Country is required")
        String country,
        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        BigDecimal latitude,
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
        BigDecimal longitude
) {}
