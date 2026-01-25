package com.damocles.fleet.fleetmanagementsystembackend.account;

import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IDriverRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IUserRepository;
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

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AccountControllerIT extends AbstractPostgresIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired TestDataFactory dataFactory;
    @Autowired IAccountRepository accountRepository;
    @Autowired IUserRepository userRepository;
    @Autowired IDriverRepository driverRepository;

    @AfterEach
    void clean() {
        driverRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void admin_can_create_update_status_and_get_account() throws Exception {
        dataFactory.createAccount("admin", "pass123", Set.of(UserRole.ADMIN));
        var user = dataFactory.createUser("account-user");

        String token = AuthTestUtils.loginAndGetToken(mvc, om, "admin", "pass123");

        var createRes = mvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(om.writeValueAsString(java.util.Map.of(
                                "login", "dispatcher1",
                                "password", "pass123",
                                "userId", user.getId(),
                                "roles", java.util.List.of("DISPATCHER")
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("dispatcher1"))
                .andReturn();

        JsonNode createJson = om.readTree(createRes.getResponse().getContentAsString());
        long accountId = createJson.get("id").asLong();

        mvc.perform(patch("/api/accounts/" + accountId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/accounts/" + accountId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void admin_cannot_remove_driver_role_from_driver_account() throws Exception {
        dataFactory.createAccount("admin", "pass123", Set.of(UserRole.ADMIN));
        var driverUser = dataFactory.createUser("driver-user");
        dataFactory.createDriver(driverUser, "LIC-500");

        String token = AuthTestUtils.loginAndGetToken(mvc, om, "admin", "pass123");

        var createRes = mvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(om.writeValueAsString(java.util.Map.of(
                                "login", "driver-acc",
                                "password", "pass123",
                                "userId", driverUser.getId(),
                                "roles", java.util.List.of("DRIVER")
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        long accountId = om.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(patch("/api/accounts/" + accountId + "/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"roles\":[\"DISPATCHER\"]}"))
                .andExpect(status().isConflict());
    }
}
