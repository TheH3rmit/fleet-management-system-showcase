package com.damocles.fleet.fleetmanagementsystembackend.worklog;

import com.damocles.fleet.fleetmanagementsystembackend.domain.ActivityType;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Driver;
import com.damocles.fleet.fleetmanagementsystembackend.domain.DriverWorkLog;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Transport;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driverWorkLog.CreateDriverWorkLogRequest;
import com.damocles.fleet.fleetmanagementsystembackend.exception.BusinessValidationException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.IDriverWorkLogMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IDriverRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IDriverWorkLogRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITransportRepository;
import com.damocles.fleet.fleetmanagementsystembackend.service.DriverWorkLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverWorkLogServiceTest {

    @Mock IDriverWorkLogRepository driverWorkLogRepository;
    @Mock IDriverRepository driverRepository;
    @Mock ITransportRepository transportRepository;
    @Mock IDriverWorkLogMapper mapper;

    private DriverWorkLogService service;

    @BeforeEach
    void setUp() {
        service = new DriverWorkLogService(
                driverWorkLogRepository,
                driverRepository,
                transportRepository,
                mapper
        );
    }

    @Test
    void createLog_sets_relations_and_saves() {
        Driver driver = new Driver();
        driver.setUserId(10L);
        Transport transport = new Transport();
        transport.setId(20L);
        transport.setDriver(driver);
        ActivityType type = ActivityType.DRIVING;

        when(driverRepository.findById(10L)).thenReturn(Optional.of(driver));
        when(transportRepository.findById(20L)).thenReturn(Optional.of(transport));
        DriverWorkLog log = new DriverWorkLog();
        when(mapper.toEntity(org.mockito.ArgumentMatchers.any())).thenReturn(log);

        CreateDriverWorkLogRequest req = new CreateDriverWorkLogRequest(
                Instant.now(),
                Instant.now(),
                15,
                "note",
                10L,
                20L,
                type
        );

        service.createLog(req);

        ArgumentCaptor<DriverWorkLog> captor = ArgumentCaptor.forClass(DriverWorkLog.class);
        verify(driverWorkLogRepository).save(captor.capture());

        DriverWorkLog saved = captor.getValue();
        assertEquals(driver, saved.getDriver());
        assertEquals(transport, saved.getTransport());
        assertEquals(type, saved.getActivityType());
    }

    @Test
    void createLog_throws_when_transport_has_no_driver() {
        Driver driver = new Driver();
        driver.setUserId(10L);
        Transport transport = new Transport();
        transport.setId(20L);
        transport.setDriver(null);
        ActivityType type = ActivityType.DRIVING;

        when(driverRepository.findById(10L)).thenReturn(Optional.of(driver));
        when(transportRepository.findById(20L)).thenReturn(Optional.of(transport));
        CreateDriverWorkLogRequest req = new CreateDriverWorkLogRequest(
                Instant.now(),
                Instant.now(),
                0,
                "note",
                10L,
                20L,
                type
        );

        assertThrows(BusinessValidationException.class, () -> service.createLog(req));
    }
}
