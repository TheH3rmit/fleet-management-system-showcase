package com.damocles.fleet.fleetmanagementsystembackend.statushistory;

import com.damocles.fleet.fleetmanagementsystembackend.domain.StatusHistory;
import com.damocles.fleet.fleetmanagementsystembackend.dto.statusHistory.StatusHistoryDTO;
import com.damocles.fleet.fleetmanagementsystembackend.exception.TransportNotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.ITransportMapper;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.IStatusHistoryMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.*;
import com.damocles.fleet.fleetmanagementsystembackend.service.TransportService;
import com.damocles.fleet.fleetmanagementsystembackend.support.TestTransportMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusHistoryServiceTest {

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
    void getStatusHistories_throws_when_transport_missing() {
        when(transportRepository.existsById(99L)).thenReturn(false);
        assertThrows(TransportNotFoundException.class, () -> service.getStatusHistories(99L));
    }

    @Test
    void getStatusHistories_maps_results() {
        when(transportRepository.existsById(1L)).thenReturn(true);
        when(statusHistoryRepository.findByTransport_IdOrderByChangedAtDesc(1L))
                .thenReturn(List.of(new StatusHistory()));
        when(statusHistoryMapper.toDto(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new StatusHistoryDTO(
                        1L,
                        1L,
                        com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.PLANNED,
                        null,
                        null,
                        null
                ));

        var result = service.getStatusHistories(1L);

        assertEquals(1, result.size());
        assertEquals(com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.PLANNED, result.get(0).status());
    }
}
