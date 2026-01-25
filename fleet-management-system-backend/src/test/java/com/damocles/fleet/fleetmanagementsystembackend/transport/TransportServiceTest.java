package com.damocles.fleet.fleetmanagementsystembackend.transport;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Driver;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Transport;
import com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.CreateTransportRequest;
import com.damocles.fleet.fleetmanagementsystembackend.exception.BusinessValidationException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.TransportNotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.IStatusHistoryMapper;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.ITransportMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.*;
import com.damocles.fleet.fleetmanagementsystembackend.service.TransportService;
import com.damocles.fleet.fleetmanagementsystembackend.support.TestTransportMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransportServiceTest {

    @Mock ITransportRepository transportRepository;
    @Mock IUserRepository userRepository;
    @Mock IVehicleRepository vehicleRepository;
    @Mock ITrailerRepository trailerRepository;
    @Mock ILocationRepository locationRepository;
    @Mock IAccountRepository accountRepository;
    @Mock IDriverRepository driverRepository;
    ITransportMapper transportMapper;
    @Mock IStatusHistoryRepository statusHistoryRepository;
    @Mock IStatusHistoryMapper statusHistoryMapper;

    private TransportService service;

    @BeforeEach
    void setUp() {
        transportMapper = new TestTransportMapper();
        service = new TransportService(
                transportRepository,
                userRepository,
                vehicleRepository,
                trailerRepository,
                locationRepository,
                accountRepository,
                driverRepository,
                transportMapper,
                statusHistoryRepository,
                statusHistoryMapper
        );
    }

    @Test
    void updateTransport_throws_when_not_planned() {
        Transport transport = new Transport();
        transport.setId(1L);
        transport.setStatus(TransportStatus.IN_PROGRESS);

        when(transportRepository.findById(1L)).thenReturn(Optional.of(transport));

        assertThrows(BusinessValidationException.class,
                () -> service.updateTransport(1L, new CreateTransportRequest(
                        null, null, null, null, null, null, null, null, null
                ), null));
    }

    @Test
    void acceptTransport_throws_when_driver_has_active_transport() {
        when(transportRepository.existsByDriver_UserIdAndStatusAndIdNot(
                10L,
                TransportStatus.IN_PROGRESS,
                99L
        )).thenReturn(true);

        assertThrows(BusinessValidationException.class, () -> service.acceptTransport(99L, 10L));
    }

    @Test
    void updateStatus_throws_when_driver_not_owner() {
        Driver assigned = new Driver();
        assigned.setUserId(10L);

        Transport transport = new Transport();
        transport.setId(1L);
        transport.setStatus(TransportStatus.PLANNED);
        transport.setDriver(assigned);

        when(transportRepository.findById(1L)).thenReturn(Optional.of(transport));

        assertThrows(BusinessValidationException.class,
                () -> service.updateStatus(1L, 99L, TransportStatus.ACCEPTED));
    }

    @Test
    void updateStatus_throws_when_invalid_transition() {
        Driver driver = new Driver();
        driver.setUserId(10L);

        Transport transport = new Transport();
        transport.setId(2L);
        transport.setStatus(TransportStatus.ACCEPTED);
        transport.setDriver(driver);

        when(transportRepository.findById(2L)).thenReturn(Optional.of(transport));

        assertThrows(BusinessValidationException.class,
                () -> service.updateStatus(2L, 10L, TransportStatus.FINISHED));
    }

    @Test
    void updateStatus_throws_when_driver_sets_forbidden_final_status() {
        assertThrows(BusinessValidationException.class,
                () -> service.updateStatus(3L, 10L, TransportStatus.CANCELLED));
    }

    @Test
    void changeStatusByAdmin_throws_when_setting_driver_only_status() {
        Transport transport = new Transport();
        transport.setId(4L);
        transport.setStatus(TransportStatus.PLANNED);

        when(transportRepository.findById(4L)).thenReturn(Optional.of(transport));

        assertThrows(BusinessValidationException.class,
                () -> service.changeStatusByAdmin(4L, TransportStatus.ACCEPTED, 1L));
    }

    @Test
    void changeStatusByAdmin_throws_when_transport_final() {
        Transport transport = new Transport();
        transport.setId(5L);
        transport.setStatus(TransportStatus.FINISHED);

        when(transportRepository.findById(5L)).thenReturn(Optional.of(transport));

        assertThrows(BusinessValidationException.class,
                () -> service.changeStatusByAdmin(5L, TransportStatus.CANCELLED, 1L));
    }

    @Test
    void assignDriver_throws_when_transport_not_planned() {
        Transport transport = new Transport();
        transport.setId(6L);
        transport.setStatus(TransportStatus.IN_PROGRESS);

        when(transportRepository.findById(6L)).thenReturn(Optional.of(transport));

        assertThrows(BusinessValidationException.class, () -> service.assignDriver(6L, 10L));
    }

    @Test
    void assignDriver_throws_when_driver_not_available() {
        Transport transport = new Transport();
        transport.setId(8L);
        transport.setStatus(TransportStatus.PLANNED);

        Driver driver = new Driver();
        driver.setUserId(10L);
        driver.setDriverStatus(com.damocles.fleet.fleetmanagementsystembackend.domain.DriverStatus.ON_TRANSPORT);

        when(transportRepository.findById(8L)).thenReturn(Optional.of(transport));
        when(driverRepository.findById(10L)).thenReturn(Optional.of(driver));

        assertThrows(BusinessValidationException.class, () -> service.assignDriver(8L, 10L));
    }

    @Test
    void acceptTransport_throws_when_transport_not_planned() {
        Driver driver = new Driver();
        driver.setUserId(10L);

        Transport transport = new Transport();
        transport.setId(9L);
        transport.setStatus(TransportStatus.ACCEPTED);
        transport.setDriver(driver);

        when(transportRepository.findById(9L)).thenReturn(Optional.of(transport));

        assertThrows(BusinessValidationException.class, () -> service.acceptTransport(9L, 10L));
    }

    @Test
    void deleteTransport_throws_when_not_planned() {
        Transport transport = new Transport();
        transport.setId(7L);
        transport.setStatus(TransportStatus.IN_PROGRESS);

        when(transportRepository.findById(7L)).thenReturn(Optional.of(transport));

        assertThrows(BusinessValidationException.class, () -> service.deleteTransport(7L));
    }

    @Test
    void updateStatus_throws_when_transport_missing() {
        when(transportRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(TransportNotFoundException.class,
                () -> service.updateStatus(99L, 10L, TransportStatus.ACCEPTED));
    }
}
