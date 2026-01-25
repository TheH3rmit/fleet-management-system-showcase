package com.damocles.fleet.fleetmanagementsystembackend.web;

import com.damocles.fleet.fleetmanagementsystembackend.dto.admin.AdminCreateUserWithAccountDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.admin.AdminCreateUserWithAccountResponseDTO;
import com.damocles.fleet.fleetmanagementsystembackend.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/users-with-account")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    // Create user and account in one admin operation.
    public AdminCreateUserWithAccountResponseDTO createUserWithAccount(
            @Valid @RequestBody AdminCreateUserWithAccountDTO dto
    ) {
        return adminService.createUserWithAccount(dto);
    }
}
