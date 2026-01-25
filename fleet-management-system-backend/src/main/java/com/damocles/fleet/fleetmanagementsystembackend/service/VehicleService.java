package com.damocles.fleet.fleetmanagementsystembackend.service;

import com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Vehicle;
import com.damocles.fleet.fleetmanagementsystembackend.domain.VehicleStatus;
import com.damocles.fleet.fleetmanagementsystembackend.dto.vehicle.CreateVehicleRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.vehicle.VehicleDTO;
import com.damocles.fleet.fleetmanagementsystembackend.exception.BusinessValidationException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.ConflictException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.NotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.VehicleNotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.IVehicleMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITransportRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IVehicleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleService implements IVehicleService {

    private final IVehicleRepository vehicleRepository;
    private final IVehicleMapper vehicleMapper;
    private final ITransportRepository transportRepository;

    @Override
    // Returns all vehicles with assignment flags.
    public List<VehicleDTO> getAllVehicles() {
        return vehicleRepository.findAll()
                .stream()
                .map(this::enrichVehicle)
                .toList();
    }

    @Override
    // Searches vehicles by free-text query with paging.
    public Page<VehicleDTO> searchVehicles(String q, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim().toLowerCase();

        if (query == null) {
            return vehicleRepository.findAll(pageable).map(this::enrichVehicle);
        }

        return vehicleRepository.search(query, pageable).map(this::enrichVehicle);
    }

    @Override
    // Fetches a vehicle by id.
    public VehicleDTO getVehicleById(Long id) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
        return enrichVehicle(v);
    }

    @Override
    // Creates a vehicle with unique license plate validation.
    public VehicleDTO createVehicle(CreateVehicleRequest req) {
        if (vehicleRepository.existsByLicensePlate(req.licensePlate())) {
            throw new ConflictException("Vehicle with license plate " + req.licensePlate() + " already exists");
        }
        Vehicle vehicle = vehicleMapper.toEntity(req);
        vehicleRepository.save(vehicle);
        return enrichVehicle(vehicle);
    }

    @Override
    // Updates vehicle fields and validates license plate uniqueness.
    public VehicleDTO updateVehicle(Long id, CreateVehicleRequest req) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        if (!v.getLicensePlate().equals(req.licensePlate()) &&
                vehicleRepository.existsByLicensePlate(req.licensePlate())) {
            throw new ConflictException("Another vehicle already uses license plate " + req.licensePlate());
        }

        vehicleMapper.updateVehicleFromDto(req, v);
        return enrichVehicle(v);
    }

    @Override
    // Deletes a vehicle only when not assigned to any transport.
    public void deleteVehicle(Long id) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
        if (transportRepository.existsByVehicle_Id(id)) {
            throw new BusinessValidationException("Vehicle is assigned to a transport and cannot be deleted");
        }
        vehicleRepository.delete(v);
    }
    @Override
    // Returns vehicles that are currently available.
    public List<VehicleDTO> getAvailableVehicles() {
        return vehicleRepository.findAvailable()
                .stream().map(this::enrichVehicle).toList();
    }

    @Transactional
    // Updates vehicle status when not assigned to active transport.
    public VehicleDTO updateStatus(Long vehicleId, VehicleStatus status) {
        Vehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found: " + vehicleId));

        if (transportRepository.existsByVehicle_IdAndStatusIn(
                vehicleId,
                List.of(TransportStatus.ACCEPTED, TransportStatus.IN_PROGRESS)
        )) {
            throw new BusinessValidationException("Vehicle is assigned to an active transport and status cannot be changed");
        }

        v.setVehicleStatus(status);
        return enrichVehicle(v);
    }

    @Override
    // Resolves a list of vehicles by id with a defensive size limit.
    public List<VehicleDTO> getVehiclesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        if (ids.size() > 200) {
            throw new BusinessValidationException("Too many ids (max 200)");
        }

        return vehicleRepository.findAllById(ids).stream()
                .map(this::enrichVehicle)
                .toList();
    }

    private VehicleDTO enrichVehicle(Vehicle v) {
        VehicleDTO base = vehicleMapper.toDto(v);
        boolean assigned = transportRepository.existsByVehicle_Id(v.getId());
        boolean inProgress = transportRepository.existsByVehicle_IdAndStatusIn(
                v.getId(),
                List.of(TransportStatus.ACCEPTED, TransportStatus.IN_PROGRESS)
        );
        return new VehicleDTO(
                base.id(),
                base.manufacturer(),
                base.model(),
                base.dateOfProduction(),
                base.mileage(),
                base.fuelType(),
                base.vehicleStatus(),
                base.licensePlate(),
                base.allowedLoad(),
                base.insuranceNumber(),
                assigned,
                inProgress
        );
    }
}
