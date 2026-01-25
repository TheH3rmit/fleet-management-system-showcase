package com.damocles.fleet.fleetmanagementsystembackend.account;

import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;
import com.damocles.fleet.fleetmanagementsystembackend.dto.account.AccountRegisterDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.account.AccountRolesUpdateDTO;
import com.damocles.fleet.fleetmanagementsystembackend.exception.BusinessValidationException;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IDriverRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IUserRepository;
import com.damocles.fleet.fleetmanagementsystembackend.service.AccountService;
import com.damocles.fleet.fleetmanagementsystembackend.support.AbstractPostgresIT;
import com.damocles.fleet.fleetmanagementsystembackend.support.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountServiceIT extends AbstractPostgresIT {

    @Autowired AccountService service;
    @Autowired TestDataFactory dataFactory;
    @Autowired IAccountRepository accountRepository;
    @Autowired IUserRepository userRepository;
    @Autowired IDriverRepository driverRepository;

    @AfterEach
    void clean() {
        driverRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void register_creates_account() {
        var user = dataFactory.createUser("acc");

        AccountRegisterDTO dto = new AccountRegisterDTO(
                "account1",
                "pass123",
                user.getId(),
                Set.of(UserRole.ADMIN)
        );

        var created = service.register(dto);

        assertEquals("account1", created.login());
    }

    @Test
    void updateRoles_throws_for_driver_without_driver_role() {
        var user = dataFactory.createUser("driver-user");
        dataFactory.createDriver(user, "LIC-900");

        var account = service.register(new AccountRegisterDTO(
                "driver-acc",
                "pass123",
                user.getId(),
                Set.of(UserRole.DRIVER)
        ));

        AccountRolesUpdateDTO update = new AccountRolesUpdateDTO(Set.of(UserRole.ADMIN));

        assertThrows(BusinessValidationException.class, () -> service.updateRoles(account.id(), update));
    }
}
