package com.damocles.fleet.fleetmanagementsystembackend.assets;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Vehicle;
import com.damocles.fleet.fleetmanagementsystembackend.domain.VehicleStatus;
import com.damocles.fleet.fleetmanagementsystembackend.exception.BusinessValidationException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.IVehicleMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITransportRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IVehicleRepository;
import com.damocles.fleet.fleetmanagementsystembackend.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock IVehicleRepository vehicleRepository;
    @Mock IVehicleMapper vehicleMapper;
    @Mock ITransportRepository transportRepository;

    private VehicleService service;

    @BeforeEach
    void setUp() {
        service = new VehicleService(vehicleRepository, vehicleMapper, transportRepository);
    }

    @Test
    void updateStatus_throws_when_vehicle_in_active_transport() {
        Vehicle v = new Vehicle();
        v.setId(10L);

        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(v));
        when(transportRepository.existsByVehicle_IdAndStatusIn(
                10L,
                java.util.List.of(
                        com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.ACCEPTED,
                        com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.IN_PROGRESS
                ))).thenReturn(true);

        assertThrows(BusinessValidationException.class, () -> service.updateStatus(10L, VehicleStatus.ACTIVE));
    }
}
