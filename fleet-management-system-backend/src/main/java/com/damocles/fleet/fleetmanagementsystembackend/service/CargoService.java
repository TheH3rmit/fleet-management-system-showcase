package com.damocles.fleet.fleetmanagementsystembackend.service;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Cargo;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Transport;
import com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus;
import com.damocles.fleet.fleetmanagementsystembackend.dto.cargo.CreateCargoRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.cargo.CargoDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.cargo.UpdateCargoRequest;
import com.damocles.fleet.fleetmanagementsystembackend.exception.BusinessValidationException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.CargoNotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.NotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.ICargoMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ICargoRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITransportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class CargoService implements ICargoService {

    private final ICargoRepository cargoRepository;
    private final ITransportRepository transportRepository;
    private final ICargoMapper cargoMapper;

    @Override
    // Returns all cargo records without filtering.
    public List<CargoDTO> getAllCargos() {
        return cargoRepository.findAll()
                .stream()
                .map(cargoMapper::toDto)
                .toList();
    }

    // Searches cargo using a free-text query and paging.
    public Page<CargoDTO> searchCargos(String q, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim().toLowerCase();

        if (query == null) {
            return cargoRepository.findAll(pageable).map(cargoMapper::toDto);
        }

        return cargoRepository.search(query, pageable).map(cargoMapper::toDto);
    }

    @Override
    // Lists all cargo assigned to a specific transport.
    public List<CargoDTO> getCargosByTransport(Long transportId) {
        return cargoRepository.findAllByTransport_Id(transportId)
                .stream()
                .map(cargoMapper::toDto)
                .toList();
    }

    @Override
    // Fetches a single cargo item by id.
    public CargoDTO getCargoById(Long id) {
        Cargo c = cargoRepository.findById(id)
                .orElseThrow(() -> new CargoNotFoundException(id));
        return cargoMapper.toDto(c);
    }

    @Override
    // Creates a cargo entry for a transport with business validation.
    public CargoDTO createCargo(CreateCargoRequest req) {
        Transport transport = transportRepository.findById(req.transportId())
                .orElseThrow(() -> new NotFoundException("Transport with ID " + req.transportId() + " not found"));
        // ALLOWED ONLY: PLANNED, ACCEPTED
        var status = transport.getStatus();
        if (status != TransportStatus.PLANNED && status != TransportStatus.ACCEPTED) {
            throw new BusinessValidationException(
                    "Cargo can be added only to PLANNED or ACCEPTED transport"
            );
        }
        validateWeight(transport, req.weightKg(), null);
        validateVolume(transport, req.volumeM3(), null);
        validateDates(req.pickupDate(), req.deliveryDate());
        Cargo cargo = cargoMapper.toEntity(req);
        cargo.setTransport(transport);
        cargoRepository.save(cargo);
        return cargoMapper.toDto(cargo);
    }

    @Override
    // Applies a partial update to cargo fields when transport status allows it.
    public CargoDTO updateCargo(Long id, UpdateCargoRequest req) {
        Cargo c = cargoRepository.findById(id)
                .orElseThrow(() -> new CargoNotFoundException(id));

        Transport transport = c.getTransport();
        var status = transport.getStatus();
        if (status != TransportStatus.PLANNED && status != TransportStatus.ACCEPTED) {
            throw new BusinessValidationException(
                    "Cargo can be edited only in PLANNED or ACCEPTED transport"
            );
        }

        if (req.weightKg() != null) {
            validateWeight(transport, req.weightKg(), c.getId());
            c.setWeightKg(req.weightKg());
        }

        if (req.volumeM3() != null) {
            validateVolume(transport, req.volumeM3(), c.getId());
            c.setVolumeM3(req.volumeM3());
        }

        Instant nextPickup = req.pickupDate() != null ? req.pickupDate() : c.getPickupDate();
        Instant nextDelivery = req.deliveryDate() != null ? req.deliveryDate() : c.getDeliveryDate();
        validateDates(nextPickup, nextDelivery);

        // manual or mapper
        if (req.cargoDescription() != null) c.setCargoDescription(req.cargoDescription().trim());
        c.setPickupDate(nextPickup);
        c.setDeliveryDate(nextDelivery);

        return cargoMapper.toDto(c);
    }


    @Override
    // Deletes cargo only when its transport is still planned.
    public void deleteCargo(Long id) {
        Cargo c = cargoRepository.findById(id)
                .orElseThrow(() -> new CargoNotFoundException(id));
        Transport transport = c.getTransport();
        if (transport != null) {
            var status = transport.getStatus();
            if (status != TransportStatus.PLANNED) {
                throw new BusinessValidationException(
                        "Cargo belongs to transport #" + transport.getId() + " in status " + status +
                                " and cannot be deleted after acceptance"
                );
            }
        }
        cargoRepository.delete(c);
    }

    // Returns cargo for all transports assigned to the driver.
    public List<CargoDTO> getCargosForDriver(Long driverId) {
        return cargoRepository.findAllByTransport_Driver_UserId(driverId)
                .stream()
                .map(cargoMapper::toDto)
                .toList();
    }

    private void validateWeight(Transport transport, BigDecimal weightKg, Long excludeCargoId) {
        if (weightKg == null || weightKg.signum() <= 0) {
            throw new BusinessValidationException("Cargo weight must be positive");
        }

        if (transport.getTrailer() == null || transport.getTrailer().getPayload() == null) {
            throw new BusinessValidationException("Transport has no trailer payload to validate cargo weight");
        }

        BigDecimal payload = transport.getTrailer().getPayload();
        BigDecimal currentTotal = cargoRepository.findAllByTransport_Id(transport.getId()).stream()
                .filter(c -> excludeCargoId == null || !Objects.equals(c.getId(), excludeCargoId))
                .map(Cargo::getWeightKg)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = currentTotal.add(weightKg);
        if (total.compareTo(payload) > 0) {
            throw new BusinessValidationException(
                    "Cargo weight exceeds trailer payload (" + payload + " kg)"
            );
        }
    }

    private void validateVolume(Transport transport, BigDecimal volumeM3, Long excludeCargoId) {
        if (volumeM3 == null || volumeM3.signum() <= 0) {
            throw new BusinessValidationException("Cargo volume must be positive");
        }

        if (transport.getTrailer() == null || transport.getTrailer().getVolume() == null) {
            throw new BusinessValidationException("Transport has no trailer volume to validate cargo volume");
        }

        BigDecimal trailerVolume = transport.getTrailer().getVolume();
        BigDecimal currentTotal = cargoRepository.findAllByTransport_Id(transport.getId()).stream()
                .filter(c -> excludeCargoId == null || !Objects.equals(c.getId(), excludeCargoId))
                .map(Cargo::getVolumeM3)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = currentTotal.add(volumeM3);
        if (total.compareTo(trailerVolume) > 0) {
            throw new BusinessValidationException(
                    "Cargo volume exceeds trailer volume (" + trailerVolume + " m3)"
            );
        }
    }

    private void validateDates(Instant pickupDate, Instant deliveryDate) {
        if (pickupDate != null && deliveryDate != null && deliveryDate.isBefore(pickupDate)) {
            throw new BusinessValidationException("Delivery date must be after pickup date");
        }
    }
}
