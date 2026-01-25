package com.damocles.fleet.fleetmanagementsystembackend.dto.loginHistory;
import java.time.Instant;
public record LoginHistoryDTO(
        Long id,
        Instant loggedAt,
        String ip,
        String userAgent,
        String result,
        Long accountId,
        String accountLogin
) {}
