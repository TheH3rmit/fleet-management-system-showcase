package com.damocles.fleet.fleetmanagementsystembackend.worklog;

import com.damocles.fleet.fleetmanagementsystembackend.domain.ActivityType;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Driver;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driverWorkLog.CreateDriverWorkLogRequest;
import com.damocles.fleet.fleetmanagementsystembackend.repository.*;
import com.damocles.fleet.fleetmanagementsystembackend.service.DriverWorkLogService;
import com.damocles.fleet.fleetmanagementsystembackend.service.TransportService;
import com.damocles.fleet.fleetmanagementsystembackend.support.AbstractPostgresIT;
import com.damocles.fleet.fleetmanagementsystembackend.support.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;

class DriverWorkLogServiceIT extends AbstractPostgresIT {

    @Autowired DriverWorkLogService service;
    @Autowired TransportService transportService;
    @Autowired TestDataFactory dataFactory;

    @Autowired IDriverWorkLogRepository workLogRepository;
    @Autowired IStatusHistoryRepository statusHistoryRepository;
    @Autowired ICargoRepository cargoRepository;
    @Autowired ITransportRepository transportRepository;
    @Autowired IDriverRepository driverRepository;
    @Autowired IAccountRepository accountRepository;
    @Autowired IUserRepository userRepository;
    @Autowired IVehicleRepository vehicleRepository;
    @Autowired ITrailerRepository trailerRepository;
    @Autowired ILocationRepository locationRepository;

    @AfterEach
    void clean() {
        workLogRepository.deleteAll();
        statusHistoryRepository.deleteAll();
        cargoRepository.deleteAll();
        transportRepository.deleteAll();
        driverRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
        vehicleRepository.deleteAll();
        trailerRepository.deleteAll();
        locationRepository.deleteAll();
    }

    @Test
    void create_and_list_logs_for_driver() {
        var admin = dataFactory.createUser("admin-user");
        var driverUser = dataFactory.createUser("driver");
        Driver driver = dataFactory.createDriver(driverUser, "LIC-222");

        var pickup = dataFactory.createLocation("pickup");
        var delivery = dataFactory.createLocation("delivery");
        var vehicle = dataFactory.createVehicle("PL-222");
        var trailer = dataFactory.createTrailer("TR-222");
        var transport = transportService.createTransport(
                new com.damocles.fleet.fleetmanagementsystembackend.dto.transport.CreateTransportRequest(
                        Instant.now().plusSeconds(3600),
                        Instant.now().plusSeconds(600),
                        Instant.now().plusSeconds(1800),
                        new java.math.BigDecimal("100"),
                        trailer.getId(),
                        vehicle.getId(),
                        pickup.getId(),
                        delivery.getId(),
                        driver.getUserId()
                ),
                admin.getId()
        );

        service.createLog(new CreateDriverWorkLogRequest(
                Instant.now().minusSeconds(600),
                Instant.now(),
                10,
                "note",
                driver.getUserId(),
                transport.id(),
                ActivityType.DRIVING
        ));

        var logs = service.getLogsByDriver(driver.getUserId());
        assertFalse(logs.isEmpty());
    }
}
