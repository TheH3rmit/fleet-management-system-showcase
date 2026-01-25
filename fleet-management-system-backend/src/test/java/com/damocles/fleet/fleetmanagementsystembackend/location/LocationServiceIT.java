package com.damocles.fleet.fleetmanagementsystembackend.location;

import com.damocles.fleet.fleetmanagementsystembackend.dto.location.CreateLocationRequest;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ILocationRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITransportRepository;
import com.damocles.fleet.fleetmanagementsystembackend.service.LocationService;
import com.damocles.fleet.fleetmanagementsystembackend.support.AbstractPostgresIT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LocationServiceIT extends AbstractPostgresIT {

    @Autowired LocationService service;
    @Autowired ILocationRepository locationRepository;
    @Autowired ITransportRepository transportRepository;

    @AfterEach
    void clean() {
        transportRepository.deleteAll();
        locationRepository.deleteAll();
    }

    @Test
    void create_and_search_locations() {
        CreateLocationRequest req = new CreateLocationRequest(
                "Main",
                "1",
                "CityX",
                "00-001",
                "PL",
                new BigDecimal("10.1"),
                new BigDecimal("20.2")
        );

        var created = service.createLocation(req);
        assertEquals("CityX", created.city());

        var page = service.searchLocations("cityx", PageRequest.of(0, 10));
        assertFalse(page.isEmpty());
    }
}
