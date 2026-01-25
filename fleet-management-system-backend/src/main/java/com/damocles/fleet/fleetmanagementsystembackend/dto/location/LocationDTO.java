package com.damocles.fleet.fleetmanagementsystembackend.dto.location;

import java.math.BigDecimal;

public record LocationDTO(
        Long id,
        String street,
        String buildingNumber,
        String city,
        String postcode,
        String country,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean usedAsPickup,
        boolean usedAsDelivery,
        boolean usedInTransport
) {}
