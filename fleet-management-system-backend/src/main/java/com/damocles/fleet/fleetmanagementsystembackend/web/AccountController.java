package com.damocles.fleet.fleetmanagementsystembackend.web;

import com.damocles.fleet.fleetmanagementsystembackend.dto.account.AccountRolesUpdateDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.account.*;
import com.damocles.fleet.fleetmanagementsystembackend.dto.view.AccountUserViewDTO;
import com.damocles.fleet.fleetmanagementsystembackend.exception.NotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.IAccountUserViewMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import com.damocles.fleet.fleetmanagementsystembackend.service.IAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final IAccountService accountService;
    private final IAccountRepository accountRepo;
    private final IAccountUserViewMapper viewMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // Register a new account (admin only).
    public AccountResponseDTO create(@Valid @RequestBody AccountRegisterDTO dto) {
        return accountService.register(dto);
    }

    @GetMapping("/{id}")
    // Fetch account by id.
    public AccountResponseDTO get(@PathVariable Long id) {
        return accountService.getById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/details")
    // Fetch account + user details for admin view.
    public AccountUserViewDTO getDetails(@PathVariable Long id) {
        var acc = accountRepo.findWithUserById(id)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        return viewMapper.toView(acc);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // Reset account password as admin.
    public void changePassword(@PathVariable Long id,
                               @Valid @RequestBody AccountAdminResetPasswordDTO dto) {
        accountService.changePasswordAdmin(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // Update account status as admin.
    public void updateStatus(@PathVariable Long id,
                             @Valid @RequestBody AccountStatusUpdateDTO dto) {
        accountService.updateStatus(id, dto);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping(params = "q")
    // Lookup account by login.
    public AccountResponseDTO getByLoginParam(@RequestParam String q) {
        return accountService.getByLogin(q)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }

    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // Update account roles as admin.
    public void updateRoles(
            @PathVariable Long id,
            @Valid @RequestBody AccountRolesUpdateDTO dto
    ) {
        accountService.updateRoles(id, dto);
    }
}
