package com.damocles.fleet.fleetmanagementsystembackend.driver;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Driver;
import com.damocles.fleet.fleetmanagementsystembackend.domain.DriverStatus;
import com.damocles.fleet.fleetmanagementsystembackend.exception.BusinessValidationException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.IDriverMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IDriverRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IDriverWorkLogRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITransportRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IUserRepository;
import com.damocles.fleet.fleetmanagementsystembackend.service.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock IDriverRepository driverRepository;
    @Mock IUserRepository userRepository;
    @Mock IDriverMapper driverMapper;
    @Mock ITransportRepository transportRepository;
    @Mock IDriverWorkLogRepository driverWorkLogRepository;

    private DriverService service;

    @BeforeEach
    void setUp() {
        service = new DriverService(
                driverRepository,
                userRepository,
                driverMapper,
                transportRepository,
                driverWorkLogRepository
        );
    }

    @Test
    void getDriversByIds_throws_when_too_many_ids() {
        var ids = LongStream.range(1, 202).boxed().toList();
        assertThrows(BusinessValidationException.class, () -> service.getDriversByIds(ids));
    }

    @Test
    void updateStatus_throws_when_driver_has_active_transport() {
        Driver driver = new Driver();
        driver.setUserId(10L);

        when(driverRepository.findByUserId(10L)).thenReturn(Optional.of(driver));
        when(transportRepository.existsActiveTransportByDriver(10L)).thenReturn(true);

        assertThrows(BusinessValidationException.class, () -> service.updateStatus(10L, DriverStatus.AVAILABLE));
    }
}
