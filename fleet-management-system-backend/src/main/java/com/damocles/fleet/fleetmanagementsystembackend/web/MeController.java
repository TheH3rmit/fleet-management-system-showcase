package com.damocles.fleet.fleetmanagementsystembackend.web;

import com.damocles.fleet.fleetmanagementsystembackend.dto.me.MeResponseDTO;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.IMeMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeController {

    private final IAccountRepository accountRepository;
    private final IMeMapper meMapper;

    @GetMapping("/me")
    // Return current authenticated user info.
    public ResponseEntity<MeResponseDTO> me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(MeResponseDTO.unauthenticated());
        }

        var acc = accountRepository.findWithUserByLoginIgnoreCase(auth.getName())
                .orElse(null);
        if (acc == null) {
            return ResponseEntity.status(404).body(MeResponseDTO.unauthenticated());
        }

        // role from entity Account.roles
        var roles = acc.getRoles().stream()
                .map(Enum::name)   // ADMIN, DISPATCHER, DRIVER
                .toList();

        var dto = new MeResponseDTO(
                true,
                auth.getName(),
                roles,
                meMapper.toShort(acc)
        );

        return ResponseEntity.ok(dto);
    }
}
