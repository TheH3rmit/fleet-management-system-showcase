package com.damocles.fleet.fleetmanagementsystembackend.location;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Location;
import com.damocles.fleet.fleetmanagementsystembackend.exception.ConflictException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.ILocationMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ILocationRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITransportRepository;
import com.damocles.fleet.fleetmanagementsystembackend.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock ILocationRepository locationRepository;
    @Mock ILocationMapper locationMapper;
    @Mock ITransportRepository transportRepository;

    private LocationService service;

    @BeforeEach
    void setUp() {
        service = new LocationService(locationRepository, locationMapper, transportRepository);
    }

    @Test
    void deleteLocation_throws_when_used_as_pickup() {
        Location loc = new Location();
        loc.setId(1L);

        when(locationRepository.findById(1L)).thenReturn(Optional.of(loc));
        when(transportRepository.existsByPickupLocation_Id(1L)).thenReturn(true);
        when(transportRepository.existsByDeliveryLocation_Id(1L)).thenReturn(false);

        assertThrows(ConflictException.class, () -> service.deleteLocation(1L));
    }
}
