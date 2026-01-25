package com.damocles.fleet.fleetmanagementsystembackend.security;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Driver;
import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;
import com.damocles.fleet.fleetmanagementsystembackend.repository.*;
import com.damocles.fleet.fleetmanagementsystembackend.service.TransportService;
import com.damocles.fleet.fleetmanagementsystembackend.support.AbstractPostgresIT;
import com.damocles.fleet.fleetmanagementsystembackend.support.AuthTestUtils;
import com.damocles.fleet.fleetmanagementsystembackend.support.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class SecurityAccessIT extends AbstractPostgresIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired TestDataFactory dataFactory;
    @Autowired TransportService transportService;

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
    void missing_token_returns_401() throws Exception {
        mvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void driver_cannot_access_admin_endpoints() throws Exception {
        dataFactory.createAccount("admin", "pass123", Set.of(UserRole.ADMIN));
        var driverAcc = dataFactory.createAccount("driver1", "pass123", Set.of(UserRole.DRIVER));
        dataFactory.createDriver(driverAcc.getUser(), "LIC-701");

        String driverToken = AuthTestUtils.loginAndGetToken(mvc, om, "driver1", "pass123");

        mvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "firstName": "John",
                                  "lastName": "Doe",
                                  "email": "john@local",
                                  "phone": "123",
                                  "birthDate": "1990-01-01"
                                }
                                """))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/transports")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void dispatcher_cannot_delete_transport() throws Exception {
        dataFactory.createAccount("dispatcher", "pass123", Set.of(UserRole.DISPATCHER));
        var adminUser = dataFactory.createUser("admin-user");
        var driverUser = dataFactory.createUser("driver-user");
        Driver driver = dataFactory.createDriver(driverUser, "LIC-702");

        var pickup = dataFactory.createLocation("pickup");
        var delivery = dataFactory.createLocation("delivery");
        var vehicle = dataFactory.createVehicle("PL-702");
        var trailer = dataFactory.createTrailer("TR-702");

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
                adminUser.getId()
        );

        String dispatcherToken = AuthTestUtils.loginAndGetToken(mvc, om, "dispatcher", "pass123");

        mvc.perform(delete("/api/transports/" + transport.id())
                        .header("Authorization", "Bearer " + dispatcherToken))
                .andExpect(status().isForbidden());
    }
}
