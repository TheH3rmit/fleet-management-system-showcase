package com.damocles.fleet.fleetmanagementsystembackend.assets;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class VehicleControllerIT extends AbstractPostgresIT {

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
    void vehicle_delete_is_blocked_when_assigned_to_transport() throws Exception {
        dataFactory.createAccount("admin", "pass123", Set.of(UserRole.ADMIN));
        String adminToken = AuthTestUtils.loginAndGetToken(mvc, om, "admin", "pass123");

        var pickup = dataFactory.createLocation("pickup");
        var delivery = dataFactory.createLocation("delivery");
        var trailer = dataFactory.createTrailer("TR-VEH");

        var vehicleRes = mvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "manufacturer": "Ford",
                                  "model": "Transit",
                                  "dateOfProduction": "2020-01-01",
                                  "mileage": 1000,
                                  "fuelType": "diesel",
                                  "vehicleStatus": "ACTIVE",
                                  "licensePlate": "PL-VEH",
                                  "allowedLoad": 1000,
                                  "insuranceNumber": "INS-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        long vehicleId = om.readTree(vehicleRes.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(post("/api/transports")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(om.writeValueAsString(java.util.Map.of(
                                "contractualDueAt", Instant.now().plusSeconds(3600).toString(),
                                "plannedStartAt", Instant.now().plusSeconds(600).toString(),
                                "plannedEndAt", Instant.now().plusSeconds(1800).toString(),
                                "plannedDistanceKm", 120,
                                "trailerId", trailer.getId(),
                                "vehicleId", vehicleId,
                                "pickupLocationId", pickup.getId(),
                                "deliveryLocationId", delivery.getId(),
                                "driverId", null
                        ))))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/vehicles/" + vehicleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }
}
