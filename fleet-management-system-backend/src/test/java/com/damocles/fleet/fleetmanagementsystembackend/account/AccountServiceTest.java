package com.damocles.fleet.fleetmanagementsystembackend.account;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Account;
import com.damocles.fleet.fleetmanagementsystembackend.domain.User;
import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;
import com.damocles.fleet.fleetmanagementsystembackend.dto.account.AccountChangePasswordDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.account.AccountRolesUpdateDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.account.AccountRegisterDTO;
import com.damocles.fleet.fleetmanagementsystembackend.exception.BusinessValidationException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.ConflictException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.ForbiddenException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.IAccountMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IUserRepository;
import com.damocles.fleet.fleetmanagementsystembackend.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock IAccountRepository accountRepo;
    @Mock IUserRepository userRepo;
    @Mock PasswordEncoder passwordEncoder;
    @Mock IAccountMapper mapper;

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(accountRepo, userRepo, passwordEncoder, mapper);
    }

    @Test
    void register_throws_when_login_exists() {
        when(accountRepo.existsByLoginIgnoreCase("admin")).thenReturn(true);

        AccountRegisterDTO dto = new AccountRegisterDTO(
                "admin",
                "secret12",
                1L,
                Set.of(UserRole.ADMIN)
        );

        assertThrows(ConflictException.class, () -> service.register(dto));
    }

    @Test
    void changePassword_throws_when_old_password_mismatch() {
        Account acc = new Account();
        acc.setPasswordHash("hash");

        when(accountRepo.findById(1L)).thenReturn(Optional.of(acc));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        AccountChangePasswordDTO dto = new AccountChangePasswordDTO("wrong", "newpass");

        assertThrows(ForbiddenException.class, () -> service.changePassword(1L, dto));
    }

    @Test
    void updateRoles_throws_when_roles_empty() {
        Account acc = new Account();
        when(accountRepo.findById(1L)).thenReturn(Optional.of(acc));

        AccountRolesUpdateDTO dto = new AccountRolesUpdateDTO(Set.of());

        assertThrows(BusinessValidationException.class, () -> service.updateRoles(1L, dto));
    }

    @Test
    void updateRoles_throws_when_driver_role_removed() {
        User user = new User();
        user.setId(1L);

        Account acc = new Account();
        acc.setUser(user);
        user.setDriver(new com.damocles.fleet.fleetmanagementsystembackend.domain.Driver());

        when(accountRepo.findById(1L)).thenReturn(Optional.of(acc));

        AccountRolesUpdateDTO dto = new AccountRolesUpdateDTO(Set.of(UserRole.ADMIN));

        assertThrows(BusinessValidationException.class, () -> service.updateRoles(1L, dto));
    }
}
