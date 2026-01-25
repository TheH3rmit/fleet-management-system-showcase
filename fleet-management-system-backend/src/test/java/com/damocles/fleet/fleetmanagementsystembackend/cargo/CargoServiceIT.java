package com.damocles.fleet.fleetmanagementsystembackend.cargo;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Driver;
import com.damocles.fleet.fleetmanagementsystembackend.dto.cargo.CreateCargoRequest;
import com.damocles.fleet.fleetmanagementsystembackend.repository.*;
import com.damocles.fleet.fleetmanagementsystembackend.service.CargoService;
import com.damocles.fleet.fleetmanagementsystembackend.service.TransportService;
import com.damocles.fleet.fleetmanagementsystembackend.support.AbstractPostgresIT;
import com.damocles.fleet.fleetmanagementsystembackend.support.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;

class CargoServiceIT extends AbstractPostgresIT {

    @Autowired CargoService cargoService;
    @Autowired TransportService transportService;
    @Autowired TestDataFactory dataFactory;

    @Autowired ICargoRepository cargoRepository;
    @Autowired IStatusHistoryRepository statusHistoryRepository;
    @Autowired ITransportRepository transportRepository;
    @Autowired IDriverRepository driverRepository;
    @Autowired IAccountRepository accountRepository;
    @Autowired IUserRepository userRepository;
    @Autowired IVehicleRepository vehicleRepository;
    @Autowired ITrailerRepository trailerRepository;
    @Autowired ILocationRepository locationRepository;

    @AfterEach
    void clean() {
        cargoRepository.deleteAll();
        statusHistoryRepository.deleteAll();
        transportRepository.deleteAll();
        driverRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
        vehicleRepository.deleteAll();
        trailerRepository.deleteAll();
        locationRepository.deleteAll();
    }

    @Test
    void create_and_list_cargo_for_transport() {
        var admin = dataFactory.createUser("admin-user");
        var driverUser = dataFactory.createUser("driver");
        Driver driver = dataFactory.createDriver(driverUser, "LIC-444");

        var pickup = dataFactory.createLocation("pickup");
        var delivery = dataFactory.createLocation("delivery");
        var vehicle = dataFactory.createVehicle("PL-444");
        var trailer = dataFactory.createTrailer("TR-444");

        var transport = transportService.createTransport(
                new com.damocles.fleet.fleetmanagementsystembackend.dto.transport.CreateTransportRequest(
                        Instant.now().plusSeconds(3600),
                        Instant.now().plusSeconds(600),
                        Instant.now().plusSeconds(1800),
                        new BigDecimal("100"),
                        trailer.getId(),
                        vehicle.getId(),
                        pickup.getId(),
                        delivery.getId(),
                        driver.getUserId()
                ),
                admin.getId()
        );

        cargoService.createCargo(new CreateCargoRequest(
                "Boxes",
                new BigDecimal("50"),
                new BigDecimal("2"),
                Instant.now(),
                Instant.now().plusSeconds(3600),
                transport.id()
        ));

        var cargos = cargoService.getCargosByTransport(transport.id());
        assertFalse(cargos.isEmpty());
    }
}
