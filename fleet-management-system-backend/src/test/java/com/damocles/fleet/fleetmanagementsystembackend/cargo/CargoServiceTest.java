package com.damocles.fleet.fleetmanagementsystembackend.cargo;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Cargo;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Trailer;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Transport;
import com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus;
import com.damocles.fleet.fleetmanagementsystembackend.dto.cargo.CreateCargoRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.cargo.UpdateCargoRequest;
import com.damocles.fleet.fleetmanagementsystembackend.exception.BusinessValidationException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.ICargoMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ICargoRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITransportRepository;
import com.damocles.fleet.fleetmanagementsystembackend.service.CargoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CargoServiceTest {

    @Mock ICargoRepository cargoRepository;
    @Mock ITransportRepository transportRepository;
    @Mock ICargoMapper cargoMapper;

    private CargoService service;

    @BeforeEach
    void setUp() {
        service = new CargoService(cargoRepository, transportRepository, cargoMapper);
    }

    @Test
    void createCargo_throws_when_transport_not_editable() {
        Transport transport = new Transport();
        transport.setId(1L);
        transport.setStatus(TransportStatus.IN_PROGRESS);

        when(transportRepository.findById(1L)).thenReturn(Optional.of(transport));

        CreateCargoRequest req = new CreateCargoRequest(
                "desc",
                new BigDecimal("10"),
                new BigDecimal("1"),
                null,
                null,
                1L
        );

        assertThrows(BusinessValidationException.class, () -> service.createCargo(req));
    }

    @Test
    void createCargo_throws_when_weight_exceeds_payload() {
        Trailer trailer = new Trailer();
        trailer.setPayload(new BigDecimal("100"));
        trailer.setVolume(new BigDecimal("10"));

        Transport transport = new Transport();
        transport.setId(2L);
        transport.setStatus(TransportStatus.PLANNED);
        transport.setTrailer(trailer);

        when(transportRepository.findById(2L)).thenReturn(Optional.of(transport));
        when(cargoRepository.findAllByTransport_Id(2L)).thenReturn(List.of());

        CreateCargoRequest req = new CreateCargoRequest(
                "desc",
                new BigDecimal("200"),
                new BigDecimal("1"),
                null,
                null,
                2L
        );

        assertThrows(BusinessValidationException.class, () -> service.createCargo(req));
    }

    @Test
    void createCargo_throws_when_volume_exceeds_trailer() {
        Trailer trailer = new Trailer();
        trailer.setPayload(new BigDecimal("100"));
        trailer.setVolume(new BigDecimal("5"));

        Transport transport = new Transport();
        transport.setId(3L);
        transport.setStatus(TransportStatus.PLANNED);
        transport.setTrailer(trailer);

        when(transportRepository.findById(3L)).thenReturn(Optional.of(transport));
        when(cargoRepository.findAllByTransport_Id(3L)).thenReturn(List.of());

        CreateCargoRequest req = new CreateCargoRequest(
                "desc",
                new BigDecimal("10"),
                new BigDecimal("10"),
                null,
                null,
                3L
        );

        assertThrows(BusinessValidationException.class, () -> service.createCargo(req));
    }

    @Test
    void createCargo_throws_when_trailer_missing() {
        Transport transport = new Transport();
        transport.setId(4L);
        transport.setStatus(TransportStatus.PLANNED);
        transport.setTrailer(null);

        when(transportRepository.findById(4L)).thenReturn(Optional.of(transport));

        CreateCargoRequest req = new CreateCargoRequest(
                "desc",
                new BigDecimal("10"),
                new BigDecimal("1"),
                null,
                null,
                4L
        );

        assertThrows(BusinessValidationException.class, () -> service.createCargo(req));
    }

    @Test
    void updateCargo_throws_when_transport_not_editable() {
        Transport transport = new Transport();
        transport.setStatus(TransportStatus.FINISHED);

        Cargo cargo = new Cargo();
        cargo.setId(5L);
        cargo.setTransport(transport);

        when(cargoRepository.findById(5L)).thenReturn(Optional.of(cargo));

        UpdateCargoRequest req = new UpdateCargoRequest(
                "desc",
                new BigDecimal("10"),
                new BigDecimal("1"),
                null,
                null
        );

        assertThrows(BusinessValidationException.class, () -> service.updateCargo(5L, req));
    }

    @Test
    void deleteCargo_throws_when_transport_not_planned() {
        Transport transport = new Transport();
        transport.setId(10L);
        transport.setStatus(TransportStatus.ACCEPTED);

        Cargo cargo = new Cargo();
        cargo.setId(11L);
        cargo.setTransport(transport);

        when(cargoRepository.findById(11L)).thenReturn(Optional.of(cargo));

        assertThrows(BusinessValidationException.class, () -> service.deleteCargo(11L));
    }
}
