package com.damocles.fleet.fleetmanagementsystembackend.cargo;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CargoControllerIT extends AbstractPostgresIT {

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
    void cargo_delete_is_blocked_after_transport_accept() throws Exception {
        dataFactory.createAccount("admin", "pass123", Set.of(UserRole.ADMIN));
        var driverAcc = dataFactory.createAccount("driver1", "pass123", Set.of(UserRole.DRIVER));
        Driver driver = dataFactory.createDriver(driverAcc.getUser(), "LIC-2");

        var pickup = dataFactory.createLocation("pickup");
        var delivery = dataFactory.createLocation("delivery");
        var vehicle = dataFactory.createVehicle("PL-456");
        var trailer = dataFactory.createTrailer("TR-456");

        String adminToken = AuthTestUtils.loginAndGetToken(mvc, om, "admin", "pass123");
        String driverToken = AuthTestUtils.loginAndGetToken(mvc, om, "driver1", "pass123");

        long transportId = createTransport(adminToken, driver.getUserId(), vehicle.getId(), trailer.getId(),
                pickup.getId(), delivery.getId());

        var cargoRes = mvc.perform(post("/api/cargos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(om.writeValueAsString(java.util.Map.of(
                                "cargoDescription", "Boxes",
                                "weightKg", 100,
                                "volumeM3", 2,
                                "pickupDate", Instant.now().toString(),
                                "deliveryDate", Instant.now().plusSeconds(3600).toString(),
                                "transportId", transportId
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        long cargoId = om.readTree(cargoRes.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(patch("/api/transports/" + transportId + "/status")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType("application/json")
                        .content("{\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/cargos/" + cargoId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }

    @Test
    void cargo_validation_rejects_negative_weight() throws Exception {
        dataFactory.createAccount("admin", "pass123", Set.of(UserRole.ADMIN));
        var driverAcc = dataFactory.createAccount("driver1", "pass123", Set.of(UserRole.DRIVER));
        Driver driver = dataFactory.createDriver(driverAcc.getUser(), "LIC-3");

        var pickup = dataFactory.createLocation("pickup");
        var delivery = dataFactory.createLocation("delivery");
        var vehicle = dataFactory.createVehicle("PL-789");
        var trailer = dataFactory.createTrailer("TR-789");

        String adminToken = AuthTestUtils.loginAndGetToken(mvc, om, "admin", "pass123");

        long transportId = createTransport(adminToken, driver.getUserId(), vehicle.getId(), trailer.getId(),
                pickup.getId(), delivery.getId());

        mvc.perform(post("/api/cargos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(om.writeValueAsString(java.util.Map.of(
                                "cargoDescription", "Boxes",
                                "weightKg", -10,
                                "volumeM3", 2,
                                "pickupDate", Instant.now().toString(),
                                "deliveryDate", Instant.now().plusSeconds(3600).toString(),
                                "transportId", transportId
                        ))))
                .andExpect(status().isBadRequest());
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
