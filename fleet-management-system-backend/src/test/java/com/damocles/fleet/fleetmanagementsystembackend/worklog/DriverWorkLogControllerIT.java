package com.damocles.fleet.fleetmanagementsystembackend.worklog;

import com.damocles.fleet.fleetmanagementsystembackend.domain.ActivityType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class DriverWorkLogControllerIT extends AbstractPostgresIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
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
    void create_update_and_list_work_logs() throws Exception {
        dataFactory.createAccount("admin", "pass123", Set.of(UserRole.ADMIN));
        var driverAcc = dataFactory.createAccount("driver1", "pass123", Set.of(UserRole.DRIVER));
        Driver driver = dataFactory.createDriver(driverAcc.getUser(), "LIC-10");

        var pickup = dataFactory.createLocation("pickup");
        var delivery = dataFactory.createLocation("delivery");
        var vehicle = dataFactory.createVehicle("PL-1010");
        var trailer = dataFactory.createTrailer("TR-1010");
        String adminToken = AuthTestUtils.loginAndGetToken(mvc, om, "admin", "pass123");

        long transportId = createTransport(adminToken, driver.getUserId(), vehicle.getId(), trailer.getId(),
                pickup.getId(), delivery.getId());

        var createRes = mvc.perform(post("/api/work-logs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(om.writeValueAsString(java.util.Map.of(
                                "startTime", Instant.now().minusSeconds(600).toString(),
                                "endTime", Instant.now().toString(),
                                "breakDuration", 15,
                                "notes", "Initial log",
                                "driverId", driver.getUserId(),
                                "transportId", transportId,
                                "activityType", ActivityType.DRIVING.name()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.notes").value("Initial log"))
                .andReturn();

        long logId = om.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(put("/api/work-logs/" + logId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(om.writeValueAsString(java.util.Map.of(
                                "startTime", Instant.now().minusSeconds(900).toString(),
                                "endTime", Instant.now().toString(),
                                "breakDuration", 30,
                                "notes", "Updated log",
                                "driverId", driver.getUserId(),
                                "transportId", transportId,
                                "activityType", ActivityType.DRIVING.name()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Updated log"));

        mvc.perform(get("/api/work-logs/driver/" + driver.getUserId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driverId").value(driver.getUserId()));
    }

    private long createTransport(String adminToken, Long driverId, Long vehicleId, Long trailerId,
                                 Long pickupId, Long deliveryId) throws Exception {
        var res = mvc.perform(post("/api/transports")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(om.writeValueAsString(java.util.Map.of(
                                "contractualDueAt", Instant.now().plusSeconds(3600).toString(),
                                "plannedStartAt", Instant.now().plusSeconds(600).toString(),
                                "plannedEndAt", Instant.now().plusSeconds(1800).toString(),
                                "plannedDistanceKm", 120,
                                "trailerId", trailerId,
                                "vehicleId", vehicleId,
                                "pickupLocationId", pickupId,
                                "deliveryLocationId", deliveryId,
                                "driverId", driverId
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = om.readTree(res.getResponse().getContentAsString());
        return json.get("id").asLong();
    }
}
