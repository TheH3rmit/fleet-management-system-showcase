package com.damocles.fleet.fleetmanagementsystembackend.driver;

import com.damocles.fleet.fleetmanagementsystembackend.dto.driver.DriverDTO;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IDriverRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IUserRepository;
import com.damocles.fleet.fleetmanagementsystembackend.service.DriverService;
import com.damocles.fleet.fleetmanagementsystembackend.support.AbstractPostgresIT;
import com.damocles.fleet.fleetmanagementsystembackend.support.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DriverServiceIT extends AbstractPostgresIT {

    @Autowired DriverService service;
    @Autowired TestDataFactory dataFactory;
    @Autowired IDriverRepository driverRepository;
    @Autowired IAccountRepository accountRepository;
    @Autowired IUserRepository userRepository;

    @AfterEach
    void clean() {
        driverRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void searchDrivers_returns_results() {
        var user = dataFactory.createUser("driver-search");
        dataFactory.createDriver(user, "LIC-111");

        var page = service.searchDrivers("test", PageRequest.of(0, 10));

        assertFalse(page.isEmpty());
        DriverDTO dto = page.getContent().get(0);
        assertNotNull(dto.userId());
    }
}
