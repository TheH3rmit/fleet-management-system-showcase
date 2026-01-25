package com.damocles.fleet.fleetmanagementsystembackend.service;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Account;
import com.damocles.fleet.fleetmanagementsystembackend.domain.AccountStatus;
import com.damocles.fleet.fleetmanagementsystembackend.domain.User;
import com.damocles.fleet.fleetmanagementsystembackend.dto.admin.AdminCreateUserWithAccountDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.admin.AdminCreateUserWithAccountResponseDTO;
import com.damocles.fleet.fleetmanagementsystembackend.exception.BusinessValidationException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.ConflictException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.EmailAlreadyUsedException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.IAccountMapper;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.IUserMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IUserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService implements IAdminService {

    private final IUserRepository userRepo;
    private final IAccountRepository accountRepo;
    private final PasswordEncoder passwordEncoder;
    private final IUserMapper userMapper;
    private final IAccountMapper accountMapper;

    // Creates a new user with account credentials in a single transaction.
    @Override
    public AdminCreateUserWithAccountResponseDTO createUserWithAccount(@Valid AdminCreateUserWithAccountDTO dto) {
        String email = normalize(dto.email());
        String login = normalize(dto.login());

        // --- atomic validation ---
        if (email == null || email.isBlank()) {
            throw new BusinessValidationException("Email is required");
        }
        if (login == null || login.isBlank()) {
            throw new BusinessValidationException("Login is required");
        }
        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        if (accountRepo.existsByLoginIgnoreCase(login)) {
            throw new ConflictException("Login already exists: " + login);
        }
        if (dto.roles() == null || dto.roles().isEmpty()) {
            throw new BusinessValidationException("Roles cannot be empty");
        }

        // --- USER ---
        User user = User.builder()
                .firstName(dto.firstName())
                .middleName(dto.middleName())
                .lastName(dto.lastName())
                .email(email)
                .phone(dto.phone())
                .birthDate(dto.birthDate())
                .build();

        //save user to get ID (FK in accounts.user_id)
        User savedUser = userRepo.save(user);

        // --- ACCOUNT ---
        Account acc = Account.builder()
                .login(login)
                .passwordHash(passwordEncoder.encode(dto.password()))
                .createdAt(Instant.now())
                .status(dto.status() != null ? dto.status() : AccountStatus.ACTIVE)
                .roles(dto.roles())
                .user(savedUser)
                .build();

        // both ways relation
        savedUser.setAccount(acc);

        Account savedAcc = accountRepo.save(acc);

        return new AdminCreateUserWithAccountResponseDTO(
                userMapper.toResponse(savedUser),
                accountMapper.toResponse(savedAcc)
        );
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
