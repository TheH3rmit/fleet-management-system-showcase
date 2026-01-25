package com.damocles.fleet.fleetmanagementsystembackend.web;

import com.damocles.fleet.fleetmanagementsystembackend.dto.loginHistory.LoginHistoryDTO;
import com.damocles.fleet.fleetmanagementsystembackend.exception.NotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import com.damocles.fleet.fleetmanagementsystembackend.service.LoginHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/login-histories")
@RequiredArgsConstructor
public class LoginHistoryController {

    private final LoginHistoryService service;
    private final IAccountRepository accountRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    // List login histories.
    public List<LoginHistoryDTO> getAll() {
        return service.getAllHistories();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    // List login histories for the current account.
    public List<LoginHistoryDTO> getMy(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return List.of();
        }

        var account = accountRepository.findByLoginIgnoreCase(auth.getName())
                .orElseThrow(() -> new NotFoundException("Account not found: " + auth.getName()));
        return service.getHistoriesByAccount(account.getId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    // Get login history by id.
    public LoginHistoryDTO getById(@PathVariable Long id) {
        return service.getHistoryById(id);
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    // List login histories for an account.
    public List<LoginHistoryDTO> getByAccount(@PathVariable Long accountId) {
        return service.getHistoriesByAccount(accountId);
    }
}
