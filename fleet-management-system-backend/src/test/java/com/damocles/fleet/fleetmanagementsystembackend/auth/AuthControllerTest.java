package com.damocles.fleet.fleetmanagementsystembackend.auth;

import com.damocles.fleet.fleetmanagementsystembackend.dto.account.AccountLoginDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.auth.AuthResponse;
import com.damocles.fleet.fleetmanagementsystembackend.security.JwtService;
import com.damocles.fleet.fleetmanagementsystembackend.service.AuthService;
import com.damocles.fleet.fleetmanagementsystembackend.web.AuthController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean AuthService authService;
    @MockitoBean JwtService jwtService;

    @Test
    void login_success_shouldReturnTokens() throws Exception {
        AuthResponse response = new AuthResponse(
                "access123",
                "refresh123",
                null
        );
        when(authService.login(org.mockito.ArgumentMatchers.eq("admin"), org.mockito.ArgumentMatchers.eq("admin"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(om.writeValueAsString(new AccountLoginDTO("admin", "admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh123"));
    }

    @Test
    void login_badCredentials_shouldReturn401() throws Exception {
        when(authService.login(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenThrow(new BadCredentialsException("bad"));

        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"login":"admin","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_invalidToken_shouldReturn401() throws Exception {
        when(authService.refresh("bad")).thenReturn(null);

        mvc.perform(post("/api/auth/refresh")
                        .contentType("application/json")
                        .content("""
                                {"refreshToken":"bad"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_validToken_shouldReturnNewAccess() throws Exception {
        when(authService.refresh("good"))
                .thenReturn(new AuthResponse("newAccess", "good", null));

        mvc.perform(post("/api/auth/refresh")
                        .contentType("application/json")
                        .content("""
                                {"refreshToken":"good"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccess"))
                .andExpect(jsonPath("$.refreshToken").value("good"));
    }

    @Test
    void logout_shouldRevokeToken() throws Exception {
        mvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer abc"))
                .andExpect(status().isOk());

        verify(authService).logout("Bearer abc");
    }
}
