package com.damocles.fleet.fleetmanagementsystembackend.web;

import com.damocles.fleet.fleetmanagementsystembackend.dto.auth.AuthResponse;
import com.damocles.fleet.fleetmanagementsystembackend.dto.account.AccountLoginDTO;
import com.damocles.fleet.fleetmanagementsystembackend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login")
    // Authenticate user and return access/refresh tokens plus account summary.
    public ResponseEntity<?> login(@RequestBody @Valid AccountLoginDTO request, HttpServletRequest httpRequest) {
        try {
            AuthResponse response = authService.login(request.login(), request.password(), httpRequest);
            return ResponseEntity.ok(response);
        } catch (org.springframework.security.authentication.BadCredentialsException ex) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }
    @PostMapping("/refresh")
    // Exchange a valid refresh token for a new access token.
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> req) {
        var refreshToken = req.get("refreshToken");
        AuthResponse response = authService.refresh(refreshToken);
        if (response == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    // Revoke access token if present.
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        authService.logout(authHeader);

        return ResponseEntity.ok().build();
    }
}
