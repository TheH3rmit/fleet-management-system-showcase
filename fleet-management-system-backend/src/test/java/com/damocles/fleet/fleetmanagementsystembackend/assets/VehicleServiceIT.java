package com.damocles.fleet.fleetmanagementsystembackend.assets;

import com.damocles.fleet.fleetmanagementsystembackend.domain.VehicleStatus;
import com.damocles.fleet.fleetmanagementsystembackend.dto.vehicle.CreateVehicleRequest;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITransportRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IVehicleRepository;
import com.damocles.fleet.fleetmanagementsystembackend.service.VehicleService;
import com.damocles.fleet.fleetmanagementsystembackend.support.AbstractPostgresIT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class VehicleServiceIT extends AbstractPostgresIT {

    @Autowired VehicleService service;
    @Autowired IVehicleRepository vehicleRepository;
    @Autowired ITransportRepository transportRepository;

    @AfterEach
    void clean() {
        transportRepository.deleteAll();
        vehicleRepository.deleteAll();
    }

    @Test
    void create_search_and_update_status() {
        var created = service.createVehicle(new CreateVehicleRequest(
                "Test",
                "Model",
                LocalDate.now(),
                1000,
                "Diesel",
                "PL-1000",
                2000,
                "INS-1"
        ));

        var page = service.searchVehicles("pl-1000", PageRequest.of(0, 10));
        assertFalse(page.isEmpty());

        var updated = service.updateStatus(created.id(), VehicleStatus.IN_SERVICE);
        assertEquals(VehicleStatus.IN_SERVICE, updated.vehicleStatus());
    }
}
