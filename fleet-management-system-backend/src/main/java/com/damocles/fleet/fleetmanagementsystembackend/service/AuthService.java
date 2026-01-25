package com.damocles.fleet.fleetmanagementsystembackend.service;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Account;
import com.damocles.fleet.fleetmanagementsystembackend.dto.account.AccountResponseDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.auth.AuthResponse;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import com.damocles.fleet.fleetmanagementsystembackend.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final IAccountRepository accountRepository;
    private final LoginHistoryService loginHistoryService;

    // Authenticates user, records login attempt, and returns tokens + account summary.
    public AuthResponse login(String login, String password, HttpServletRequest request) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(login, password));

            Account account = accountRepository.findByLoginIgnoreCase(login)
                    .orElseThrow(() -> new UsernameNotFoundException("Account not found"));

            account.setLastLoginAt(Instant.now());

            List<String> roles = account.getRoles().stream()
                    .map(Enum::name)
                    .toList();

            List<GrantedAuthority> granted = roles.stream()
                    .map(r -> (GrantedAuthority) () -> "ROLE_" + r)
                    .toList();

            String accessToken = jwtService.generateToken(account.getLogin(), granted);
            String refreshToken = jwtService.generateRefreshToken(account.getLogin(), roles);

            String ipHash = request.getRemoteAddr();
            String userAgentHash = request.getHeader("User-Agent");
            loginHistoryService.logAttempt(account.getId(), "SUCCESS", ipHash, userAgentHash);

            return new AuthResponse(
                    accessToken,
                    refreshToken,
                    new AccountResponseDTO(
                            account.getId(),
                            account.getLogin(),
                            account.getStatus(),
                            account.getCreatedAt(),
                            account.getLastLoginAt(),
                            account.getUser().getId(),
                            account.getRoles()
                    )
            );
        } catch (BadCredentialsException ex) {
            accountRepository.findByLoginIgnoreCase(login).ifPresent(acc -> {
                String ipHash = request.getRemoteAddr();
                String userAgentHash = request.getHeader("User-Agent");
                loginHistoryService.logAttempt(acc.getId(), "FAILURE", ipHash, userAgentHash);
            });
            throw ex;
        }
    }

    // Exchanges a valid refresh token for a new access token.
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null || !jwtService.validateRefreshToken(refreshToken)) {
            return null;
        }
        String username = jwtService.getSubject(refreshToken);
        List<String> roles = jwtService.getRoles(refreshToken);
        List<GrantedAuthority> authorities = roles.stream()
                .map(r -> (GrantedAuthority) () -> "ROLE_" + r)
                .toList();

        String accessToken = jwtService.generateToken(username, authorities);
        return new AuthResponse(accessToken, refreshToken, null);
    }

    // Revokes access token if present.
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            jwtService.revokeToken(token);
        }
    }
}
