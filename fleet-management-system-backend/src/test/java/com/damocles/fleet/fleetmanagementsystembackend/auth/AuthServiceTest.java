package com.damocles.fleet.fleetmanagementsystembackend.auth;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Account;
import com.damocles.fleet.fleetmanagementsystembackend.domain.AccountStatus;
import com.damocles.fleet.fleetmanagementsystembackend.domain.User;
import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;
import com.damocles.fleet.fleetmanagementsystembackend.dto.auth.AuthResponse;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import com.damocles.fleet.fleetmanagementsystembackend.security.JwtService;
import com.damocles.fleet.fleetmanagementsystembackend.service.AuthService;
import com.damocles.fleet.fleetmanagementsystembackend.service.LoginHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthenticationManager authManager;
    @Mock JwtService jwtService;
    @Mock IAccountRepository accountRepository;
    @Mock LoginHistoryService loginHistoryService;
    @Mock HttpServletRequest httpRequest;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(authManager, jwtService, accountRepository, loginHistoryService);
    }

    @Test
    void login_success_returns_tokens_and_logs_success() {
        User user = new User();
        user.setId(1L);

        Account account = new Account();
        account.setId(10L);
        account.setLogin("admin");
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(Instant.now());
        account.setUser(user);
        account.setRoles(Set.of(UserRole.ADMIN));

        when(authManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("admin", "x"));
        when(accountRepository.findByLoginIgnoreCase("admin"))
                .thenReturn(Optional.of(account));
        when(jwtService.generateToken(eq("admin"), anyCollection())).thenReturn("access");
        when(jwtService.generateRefreshToken(eq("admin"), anyList())).thenReturn("refresh");
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("UA");

        AuthResponse response = service.login("admin", "pass", httpRequest);

        assertNotNull(response);
        assertEquals("access", response.accessToken());
        assertEquals("refresh", response.refreshToken());
        verify(loginHistoryService).logAttempt(10L, "SUCCESS", "127.0.0.1", "UA");
    }

    @Test
    void login_bad_credentials_logs_failure_when_account_exists() {
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        when(accountRepository.findByLoginIgnoreCase("admin"))
                .thenReturn(Optional.of(new Account() {{
                    setId(77L);
                }}));
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("UA");

        assertThrows(BadCredentialsException.class, () -> service.login("admin", "bad", httpRequest));
        verify(loginHistoryService).logAttempt(77L, "FAILURE", "127.0.0.1", "UA");
    }

    @Test
    void refresh_invalid_token_returns_null() {
        when(jwtService.validateRefreshToken("bad")).thenReturn(false);
        assertNull(service.refresh("bad"));
        verify(jwtService, never()).generateToken(any(), anyCollection());
    }

    @Test
    void refresh_valid_token_returns_new_access() {
        when(jwtService.validateRefreshToken("good")).thenReturn(true);
        when(jwtService.getSubject("good")).thenReturn("admin");
        when(jwtService.getRoles("good")).thenReturn(List.of("ADMIN"));
        when(jwtService.generateToken(eq("admin"), anyCollection())).thenReturn("newAccess");

        AuthResponse response = service.refresh("good");

        assertNotNull(response);
        assertEquals("newAccess", response.accessToken());
        assertEquals("good", response.refreshToken());
    }

    @Test
    void logout_revokes_token_when_present() {
        service.logout("Bearer abc");
        verify(jwtService).revokeToken("abc");
    }

    @Test
    void logout_ignores_missing_header() {
        service.logout(null);
        verify(jwtService, never()).revokeToken(any());
    }
}
