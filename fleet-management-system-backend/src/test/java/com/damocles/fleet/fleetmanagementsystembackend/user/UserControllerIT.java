package com.damocles.fleet.fleetmanagementsystembackend.user;

import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IUserRepository;
import com.damocles.fleet.fleetmanagementsystembackend.support.AbstractPostgresIT;
import com.damocles.fleet.fleetmanagementsystembackend.support.AuthTestUtils;
import com.damocles.fleet.fleetmanagementsystembackend.support.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class UserControllerIT extends AbstractPostgresIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired TestDataFactory dataFactory;
    @Autowired IAccountRepository accountRepository;
    @Autowired IUserRepository userRepository;

    @AfterEach
    void clean() {
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void admin_can_create_list_update_and_delete_user() throws Exception {
        dataFactory.createAccount("admin", "pass123", Set.of(UserRole.ADMIN));
        String token = AuthTestUtils.loginAndGetToken(mvc, om, "admin", "pass123");

        var createRes = mvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "firstName": "John",
                                  "middleName": null,
                                  "lastName": "Doe",
                                  "email": "john@local",
                                  "phone": "123",
                                  "birthDate": "1990-01-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("john@local"))
                .andReturn();

        Long userId = om.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .param("q", "john")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mvc.perform(patch("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "firstName": "Johnny",
                                  "email": "johnny@local"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.email").value("johnny@local"));

        mvc.perform(delete("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    
}
