package com.damocles.fleet.fleetmanagementsystembackend.dto.auth;

import com.damocles.fleet.fleetmanagementsystembackend.dto.account.AccountResponseDTO;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        AccountResponseDTO account
) { }
