package com.damocles.fleet.fleetmanagementsystembackend.assets;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Trailer;
import com.damocles.fleet.fleetmanagementsystembackend.domain.TrailerStatus;
import com.damocles.fleet.fleetmanagementsystembackend.exception.BusinessValidationException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.ITrailerMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITrailerRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITransportRepository;
import com.damocles.fleet.fleetmanagementsystembackend.service.TrailerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrailerServiceTest {

    @Mock ITrailerRepository trailerRepository;
    @Mock ITrailerMapper trailerMapper;
    @Mock ITransportRepository transportRepository;

    private TrailerService service;

    @BeforeEach
    void setUp() {
        service = new TrailerService(trailerRepository, trailerMapper, transportRepository);
    }

    @Test
    void updateStatus_throws_when_trailer_in_active_transport() {
        Trailer t = new Trailer();
        t.setId(10L);

        when(trailerRepository.findById(10L)).thenReturn(Optional.of(t));
        when(transportRepository.existsByTrailer_IdAndStatusIn(
                10L,
                java.util.List.of(
                        com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.ACCEPTED,
                        com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.IN_PROGRESS
                ))).thenReturn(true);

        assertThrows(BusinessValidationException.class, () -> service.updateStatus(10L, TrailerStatus.ACTIVE));
    }
}
