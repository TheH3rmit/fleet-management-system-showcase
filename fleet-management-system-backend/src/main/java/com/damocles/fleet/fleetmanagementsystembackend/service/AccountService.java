package com.damocles.fleet.fleetmanagementsystembackend.service;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Account;
import com.damocles.fleet.fleetmanagementsystembackend.domain.AccountStatus;
import com.damocles.fleet.fleetmanagementsystembackend.domain.User;
import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;
import com.damocles.fleet.fleetmanagementsystembackend.dto.account.AccountRolesUpdateDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.account.*;
import com.damocles.fleet.fleetmanagementsystembackend.exception.*;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.IAccountMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements IAccountService {

    private final IAccountRepository accountRepo;
    private final IUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final IAccountMapper mapper;

    @Override
    // Register a new account with user link and validation.
    public AccountResponseDTO register(AccountRegisterDTO dto) {
        String login = normalize(dto.login());
        if (login == null || login.isBlank()) {
            throw new BusinessValidationException("Login is required");
        }
        if (accountRepo.existsByLoginIgnoreCase(login)) {
            throw new ConflictException("Login already exists: " + login);
        }

        User user = userRepo.findById(dto.userId())
                .orElseThrow(() -> new UserNotFoundException(dto.userId()));

        if (accountRepo.findByUser_Id(user.getId()).isPresent()) {
            throw new ConflictException("User already has an account");
        }


        Account acc = mapper.toEntity(dto);

        acc.setLogin(login);
        acc.setRoles(dto.roles());
        acc.setPasswordHash(passwordEncoder.encode(dto.password()));
        acc.setCreatedAt(Instant.now());
        acc.setStatus(AccountStatus.ACTIVE);
        acc.setUser(user);

        user.setAccount(acc);

        Account saved = accountRepo.save(acc);
        return mapper.toResponse(saved);
    }

    @Override
    // Change password for current account (with old password check).
    public void changePassword(Long accountId, AccountChangePasswordDTO dto) {
        Account acc = accountRepo.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        String oldPw = dto.oldPassword();
        if (oldPw != null && !oldPw.isBlank()) {
            boolean ok = passwordEncoder.matches(oldPw, acc.getPasswordHash());
            if (!ok) throw new ForbiddenException("Old password mismatch");
        }

        acc.setPasswordHash(passwordEncoder.encode(dto.newPassword()));
    }

    // Reset password as admin.
    public void changePasswordAdmin(Long accountId, AccountAdminResetPasswordDTO dto) {
        Account acc = accountRepo.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        acc.setPasswordHash(passwordEncoder.encode(dto.newPassword()));
    }

    @Override
    // Update account status.
    public void updateStatus(Long accountId, AccountStatusUpdateDTO dto) {
        Account acc = accountRepo.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        acc.setStatus(dto.status());
    }

    @Override
    // Fetch account by id.
    public AccountResponseDTO getById(Long id) {
        Account acc = accountRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        return mapper.toResponse(acc);
    }

    @Override
    // Lookup account by login.
    public Optional<AccountResponseDTO> getByLogin(String login) {
        String normalized = normalize(login);
        if (normalized == null || normalized.isBlank()) {
            return Optional.empty();
        }
        return accountRepo.findByLoginIgnoreCase(normalized).map(mapper::toResponse);
    }

    @Override
    // Update last login timestamp.
    public void touchLastLogin(Long accountId) {
        accountRepo.findById(accountId)
                .ifPresent(a -> a.setLastLoginAt(Instant.now()));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    @Transactional
    // Update account roles with validation.
    public void updateRoles(Long accountId, AccountRolesUpdateDTO dto) {

        var acc = accountRepo.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        // Validation
        if (dto.roles() == null || dto.roles().isEmpty()) {
            throw new BusinessValidationException("Roles cannot be empty");
        }

        // additional validation: driver must have role driver
        if (acc.getUser().getDriver() != null &&
                !dto.roles().contains(UserRole.DRIVER)) {
            throw new BusinessValidationException("User is a driver and must have DRIVER role");
        }

        acc.setRoles(dto.roles());
        accountRepo.save(acc);
    }
}
