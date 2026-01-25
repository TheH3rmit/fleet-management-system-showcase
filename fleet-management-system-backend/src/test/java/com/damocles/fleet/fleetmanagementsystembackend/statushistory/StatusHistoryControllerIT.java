package com.damocles.fleet.fleetmanagementsystembackend.statushistory;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Driver;
import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;
import com.damocles.fleet.fleetmanagementsystembackend.repository.*;
import com.damocles.fleet.fleetmanagementsystembackend.support.AbstractPostgresIT;
import com.damocles.fleet.fleetmanagementsystembackend.support.AuthTestUtils;
import com.damocles.fleet.fleetmanagementsystembackend.support.TestDataFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class StatusHistoryControllerIT extends AbstractPostgresIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired TestDataFactory dataFactory;

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
    void admin_can_fetch_status_history_for_transport() throws Exception {
        dataFactory.createAccount("admin", "pass123", Set.of(UserRole.ADMIN));
        var driverAcc = dataFactory.createAccount("driver1", "pass123", Set.of(UserRole.DRIVER));
        Driver driver = dataFactory.createDriver(driverAcc.getUser(), "LIC-777");

        var pickup = dataFactory.createLocation("pickup");
        var delivery = dataFactory.createLocation("delivery");
        var vehicle = dataFactory.createVehicle("PL-777");
        var trailer = dataFactory.createTrailer("TR-777");

        String adminToken = AuthTestUtils.loginAndGetToken(mvc, om, "admin", "pass123");

        var createRes = mvc.perform(post("/api/transports")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(om.writeValueAsString(java.util.Map.of(
                                "contractualDueAt", Instant.now().plusSeconds(3600).toString(),
                                "plannedStartAt", Instant.now().plusSeconds(600).toString(),
                                "plannedEndAt", Instant.now().plusSeconds(1800).toString(),
                                "plannedDistanceKm", 120,
                                "trailerId", trailer.getId(),
                                "vehicleId", vehicle.getId(),
                                "pickupLocationId", pickup.getId(),
                                "deliveryLocationId", delivery.getId(),
                                "driverId", driver.getUserId()
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = om.readTree(createRes.getResponse().getContentAsString());
        long transportId = json.get("id").asLong();

        mvc.perform(get("/api/transports/" + transportId + "/history")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PLANNED"));
    }
}
