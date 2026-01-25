package com.damocles.fleet.fleetmanagementsystembackend.dto.me;

import com.damocles.fleet.fleetmanagementsystembackend.dto.account.AccountUserShortDTO;
import java.util.List;

public record MeResponseDTO(
        boolean authenticated,
        String username,
        List<String> roles,
        AccountUserShortDTO account
) {
    public static MeResponseDTO unauthenticated() {

        return new MeResponseDTO(false, null, List.of(), null);
    }
}
