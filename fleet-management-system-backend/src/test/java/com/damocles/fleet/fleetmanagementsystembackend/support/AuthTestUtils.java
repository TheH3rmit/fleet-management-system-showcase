package com.damocles.fleet.fleetmanagementsystembackend.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class AuthTestUtils {
    private AuthTestUtils() {}

    public static String loginAndGetToken(MockMvc mvc, ObjectMapper om, String login, String password) throws Exception {
        var res = mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(om.writeValueAsString(Map.of(
                                "login", login,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = om.readTree(res.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }
}
