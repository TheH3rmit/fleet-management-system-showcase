package com.damocles.fleet.fleetmanagementsystembackend.service;

import com.damocles.fleet.fleetmanagementsystembackend.domain.*;
import com.damocles.fleet.fleetmanagementsystembackend.dto.statusHistory.StatusHistoryDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.CreateTransportRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.TransportDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.TransportDetailsDTO;
import com.damocles.fleet.fleetmanagementsystembackend.exception.BusinessValidationException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.NotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.TransportNotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.IStatusHistoryMapper;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.ITransportMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.*;
import com.damocles.fleet.fleetmanagementsystembackend.util.TransportSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransportService implements ITransportService {

    private final ITransportRepository transportRepository;
    private final IUserRepository userRepository;
    private final IVehicleRepository vehicleRepository;
    private final ITrailerRepository trailerRepository;
    private final ILocationRepository locationRepository;
    private final IAccountRepository accountRepository;
    private final IDriverRepository driverRepository;

    private final ITransportMapper transportMapper;
    private final IStatusHistoryRepository statusHistoryRepository;
    private final IStatusHistoryMapper statusHistoryMapper;

    // ------------------------------
    // Basic CRUD
    // ------------------------------

    @Override
    // Returns all transports without filtering.
    public List<TransportDTO> getAllTransports() {
        return transportRepository.findAll()
                .stream()
                .map(transportMapper::toDto)
                .toList();
    }

    @Override
    // Fetches a transport by id.
    public TransportDTO getTransportById(Long id) {
        Transport transport = transportRepository.findById(id)
                .orElseThrow(() -> new TransportNotFoundException(id));
        return transportMapper.toDto(transport);
    }

    // Returns detailed transport data by id.
    public TransportDetailsDTO getTransportDetails(Long id) {
        Transport t = transportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transport not found: " + id));
        return transportMapper.toDetailsDTO(t);
    }

    // Creates a planned transport and writes initial status history.
    public TransportDTO createTransport(CreateTransportRequest req, Long createdByUserId) {
        validatePlannedDates(req);
        var user = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new NotFoundException("User id not found: " + createdByUserId));

        validateVehicleAvailable(req.vehicleId());
        validateTrailerAvailable(req.trailerId());
        validateDriverAvailable(req.driverId());

        var transport = transportMapper.toEntity(req);

        transport.setCreatedBy(user);

        transport.setVehicle(vehicleRepository.findById(req.vehicleId())
                .orElseThrow(() -> new NotFoundException("Vehicle not found: " + req.vehicleId())));

        if (req.trailerId() != null) {
            transport.setTrailer(trailerRepository.findById(req.trailerId())
                    .orElseThrow(() -> new NotFoundException("Trailer not found: " + req.trailerId())));
        } else {
            transport.setTrailer(null);
        }

        transport.setPickupLocation(locationRepository.findById(req.pickupLocationId())
                .orElseThrow(() -> new NotFoundException("Pickup location not found: " + req.pickupLocationId())));

        transport.setDeliveryLocation(locationRepository.findById(req.deliveryLocationId())
                .orElseThrow(() -> new NotFoundException("Delivery location not found: " + req.deliveryLocationId())));

        if (req.driverId() != null) {
            transport.setDriver(driverRepository.findById(req.driverId())
                    .orElseThrow(() -> new NotFoundException("Driver not found: " + req.driverId())));
        } else {
            transport.setDriver(null);
        }

        transport.setStatus(TransportStatus.PLANNED);

        var saved = transportRepository.save(transport);

        saveHistory(saved, saved.getStatus(), user);

        return transportMapper.toDto(saved);
    }

    @Override
    // Updates a transport when status is PLANNED and role allows it.
    public TransportDTO updateTransport(Long id, CreateTransportRequest req, Authentication auth) {
        Transport transport = transportRepository.findById(id)
                .orElseThrow(() -> new TransportNotFoundException(id));

        if (transport.getStatus() != TransportStatus.PLANNED) {
            throw new BusinessValidationException("Only PLANNED transports can be edited");
        }

        validatePlannedDates(req);

        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        boolean isDispatcher = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_DISPATCHER"));


        // Business rule: Dispatcher only PLANNED
        if (isDispatcher && !isAdmin) {
            if (transport.getStatus() != TransportStatus.PLANNED) {
                throw new BusinessValidationException("Dispatcher can edit only PLANNED transports");
            }
        }

        // Updating fields (dates, distance, notes, etc.).
        transportMapper.updateFromDto(req, transport);

        // vehicle: validation only on change
        Long oldVehicleId = transport.getVehicle() != null ? transport.getVehicle().getId() : null;
        if (req.vehicleId() != null && !req.vehicleId().equals(oldVehicleId)) {
            validateVehicleAvailable(req.vehicleId());
            var vehicle = vehicleRepository.findById(req.vehicleId())
                    .orElseThrow(() -> new NotFoundException("Vehicle not found: " + req.vehicleId()));
            transport.setVehicle(vehicle);
        }

        // trailer: validation only on change
        Long oldTrailerId = transport.getTrailer() != null ? transport.getTrailer().getId() : null;
        if (req.trailerId() == null) {
            transport.setTrailer(null);
        } else if (!req.trailerId().equals(oldTrailerId)) {
            validateTrailerAvailable(req.trailerId());
            var trailer = trailerRepository.findById(req.trailerId())
                    .orElseThrow(() -> new NotFoundException("Trailer not found: " + req.trailerId()));
            transport.setTrailer(trailer);
        }

        if (req.pickupLocationId() == null || req.deliveryLocationId() == null) {
            throw new BusinessValidationException("pickupLocationId and deliveryLocationId are required");
        }

        // locations must always exist (entity optional=false)
        var pickup = locationRepository.findById(req.pickupLocationId())
                .orElseThrow(() -> new NotFoundException("Pickup location not found: " + req.pickupLocationId()));
        var delivery = locationRepository.findById(req.deliveryLocationId())
                .orElseThrow(() -> new NotFoundException("Delivery location not found: " + req.deliveryLocationId()));
        transport.setPickupLocation(pickup);
        transport.setDeliveryLocation(delivery);

        //driver not assigned here on purpose
        //assigning driver via assignDriver
        return transportMapper.toDto(transport);
    }

    @Override
    // Deletes a transport only when PLANNED.
    public void deleteTransport(Long id) {
        Transport t = transportRepository.findById(id)
                .orElseThrow(() -> new TransportNotFoundException(id));
        if (t.getStatus() != TransportStatus.PLANNED) {
            throw new BusinessValidationException("Only PLANNED transports can be deleted");
        }
        transportRepository.delete(t);
    }

    @Override
    // Assigns a driver to a planned transport.
    public TransportDTO assignDriver(Long transportId, Long driverId) {
        var transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new TransportNotFoundException(transportId));

        if (transport.getStatus() != TransportStatus.PLANNED) {
            throw new BusinessValidationException("Driver can be assigned only in PLANNED status");
        }

        validateDriverAvailable(driverId);

        var driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new NotFoundException("Driver not found: " + driverId));

        transport.setDriver(driver);

        return transportMapper.toDto(transport);
    }

    // Lists transports assigned to a driver.
    public List<TransportDTO> getTransportsForDriver(Long driverId) {
        return transportRepository.findByDriver(driverId)
                .stream()
                .map(transportMapper::toDto)
                .toList();
    }

    // Allows admin to change transport status with business rules.
    public TransportDTO changeStatusByAdmin(Long id, TransportStatus next, Long changedByUserId) {

        var t = transportRepository.findById(id)
                .orElseThrow(() -> new TransportNotFoundException(id));

        TransportStatus current = t.getStatus();

        // blocking status change if the transport is in FINISHED state
        if (current == TransportStatus.FINISHED ||
                current == TransportStatus.CANCELLED ||
                current == TransportStatus.FAILED ||
                current == TransportStatus.REJECTED) {
            throw new BusinessValidationException("Cannot change status from final state: " + current);
        }

        // Admin can change status from any state
        // FINISHED, CANCELLED, FAILED, REJECTED
        // but can't use driver only states outside the driver logic (can't ACCEPTED or make it IN_PROGRESS)
        if (next == TransportStatus.ACCEPTED ||
                next == TransportStatus.IN_PROGRESS) {
            throw new BusinessValidationException("Admin cannot set intermediate driver-only status: " + next);
        }

        // Status change
        t.setStatus(next);

        // Set dates
        if (next == TransportStatus.IN_PROGRESS && t.getActualStartAt() == null) {
            t.setActualStartAt(Instant.now());
        }

        // if admin sets final states - set actualEndAt
        if ((next == TransportStatus.FINISHED ||
                next == TransportStatus.CANCELLED ||
                next == TransportStatus.FAILED ||
                next == TransportStatus.REJECTED) && t.getActualEndAt() == null) {
            t.setActualEndAt(Instant.now());
        }

        // History
        saveHistory(t, next, changedByUserId);

        return transportMapper.toDto(t);
    }

    private void ensureTransition(TransportStatus current, TransportStatus next) {
        switch (current) {
            case PLANNED -> {
                if (next != TransportStatus.ACCEPTED)
                    throw new BusinessValidationException("PLANNED -> ACCEPTED only");
            }
            case ACCEPTED -> {
                if (next != TransportStatus.IN_PROGRESS)
                    throw new BusinessValidationException("ACCEPTED -> IN_PROGRESS only");
            }
            case IN_PROGRESS -> {
                if (next != TransportStatus.FINISHED)
                    throw new BusinessValidationException("IN_PROGRESS -> FINISHED only");
            }
            case FINISHED, CANCELLED, FAILED, REJECTED -> {
                throw new BusinessValidationException("Transport in " + current + " cannot change status");
            }
        }
    }


    // Driver section

    // Driver accepts a planned transport.
    public TransportDTO acceptTransport(Long id, Long driverId) {
        // Business rule: a driver can have only one in-progress transport.
        if (transportRepository.existsByDriver_UserIdAndStatusAndIdNot(driverId, TransportStatus.IN_PROGRESS, id)) {
            throw new BusinessValidationException("Driver already has a transport in progress");
        }
        var t = changeStatus(id, driverId, TransportStatus.ACCEPTED);
        return transportMapper.toDto(t);
    }

    // Driver starts an accepted transport.
    public TransportDTO startTransport(Long id, Long driverId) {
        var t = changeStatus(id, driverId, TransportStatus.IN_PROGRESS);
        return transportMapper.toDto(t);
    }

    // Driver updates transport status within allowed transitions.
    public TransportDTO updateStatus(Long id, Long driverId, TransportStatus next) {

        //Driver can only make one way change to ACCEPTED / IN_PROGRESS / FINISHED
        //Cannot change status to CANCELLED FAILED OR REJECTED final states
        if (next == TransportStatus.CANCELLED
                || next == TransportStatus.FAILED
                || next == TransportStatus.REJECTED) {
            throw new BusinessValidationException("Driver cannot set status " + next);
        }

        if (next == TransportStatus.ACCEPTED
                && transportRepository.existsByDriver_UserIdAndStatusAndIdNot(driverId, TransportStatus.IN_PROGRESS, id)) {
            throw new BusinessValidationException("Driver already has a transport in progress");
        }

        var t = changeStatus(id, driverId, next);
        return transportMapper.toDto(t);
    }

    // Driver finishes an in-progress transport.
    public TransportDTO finishTransport(Long id, Long driverId) {
        var t = changeStatus(id, driverId, TransportStatus.FINISHED);
        return transportMapper.toDto(t);
    }

    //utility

    private Transport validateDriverOwnership(Long transportId, Long driverId) {
        var t = transportRepository.findById(transportId)
                .orElseThrow(() -> new TransportNotFoundException(transportId));

        if (t.getDriver() == null || !t.getDriver().getUserId().equals(driverId))
            throw new BusinessValidationException("Transport not assigned to this driver");

        return t;
    }

    // Resolves user id for a given account login.
    public Long getUserIdByAccountLogin(String login) {
        var acc = accountRepository.findByLoginIgnoreCase(login)
                .orElseThrow(() -> new BusinessValidationException("Account not found"));
        return acc.getUser().getId();
    }

    // Resolves driver id for a given account login.
    public Long getDriverIdByAccountLogin(String login) {
        var acc = accountRepository.findByLoginIgnoreCase(login)
                .orElseThrow(() -> new BusinessValidationException("Account not found"));

        var driver = acc.getUser().getDriver();
        if (driver == null)
            throw new BusinessValidationException("User is not a driver");

        return driver.getUserId();
    }

    private Transport changeStatus(Long transportId, Long driverId, TransportStatus next) {
        var t = validateDriverOwnership(transportId, driverId);
        var current = t.getStatus();

        ensureTransition(current, next);

        if (next == TransportStatus.IN_PROGRESS) {
            if (transportRepository.existsByDriver_UserIdAndStatusAndIdNot(driverId, TransportStatus.IN_PROGRESS, transportId)) {
                throw new BusinessValidationException("Driver already has a transport in progress");
            }
        }

        t.setStatus(next);

        if (next == TransportStatus.IN_PROGRESS && t.getActualStartAt() == null) {
            t.setActualStartAt(Instant.now());
        }

        if (next == TransportStatus.FINISHED && t.getActualEndAt() == null) {
            t.setActualEndAt(Instant.now());
        }

        saveHistory(t, next, driverId);

        return t;
    }

    // Lists status history entries for a transport.
    public List<StatusHistoryDTO> getStatusHistories(Long transportId) {
        if (!transportRepository.existsById(transportId)) {
            throw new TransportNotFoundException(transportId);
        }

        return statusHistoryRepository.findByTransport_IdOrderByChangedAtDesc(transportId)
                .stream()
                .map(statusHistoryMapper::toDto)
                .toList();
    }

    @Override
    // Admin-only update of a planned transport (same rules as dispatcher).
    public TransportDTO adminUpdateTransport(Long id, CreateTransportRequest req) {
        Transport transport = transportRepository.findById(id)
                .orElseThrow(() -> new TransportNotFoundException(id));

        if (transport.getStatus() != TransportStatus.PLANNED) {
            throw new BusinessValidationException("Only PLANNED transports can be edited");
        }

        validatePlannedDates(req);

        // Updating fields (dates, distance, notes, etc.).
        transportMapper.updateFromDto(req, transport);

        // vehicle: validation only on change
        Long oldVehicleId = transport.getVehicle() != null ? transport.getVehicle().getId() : null;
        if (req.vehicleId() != null && !req.vehicleId().equals(oldVehicleId)) {
            validateVehicleAvailable(req.vehicleId());
            var vehicle = vehicleRepository.findById(req.vehicleId())
                    .orElseThrow(() -> new NotFoundException("Vehicle not found: " + req.vehicleId()));
            transport.setVehicle(vehicle);
        }

        // trailer: validation only on change
        Long oldTrailerId = transport.getTrailer() != null ? transport.getTrailer().getId() : null;
        if (req.trailerId() == null) {
            transport.setTrailer(null);
        } else if (!req.trailerId().equals(oldTrailerId)) {
            validateTrailerAvailable(req.trailerId());
            var trailer = trailerRepository.findById(req.trailerId())
                    .orElseThrow(() -> new NotFoundException("Trailer not found: " + req.trailerId()));
            transport.setTrailer(trailer);
        }

        if (req.pickupLocationId() == null || req.deliveryLocationId() == null) {
            throw new BusinessValidationException("pickupLocationId and deliveryLocationId are required");
        }

        // locations must always exist (entity optional=false)
        var pickup = locationRepository.findById(req.pickupLocationId())
                .orElseThrow(() -> new NotFoundException("Pickup location not found: " + req.pickupLocationId()));
        var delivery = locationRepository.findById(req.deliveryLocationId())
                .orElseThrow(() -> new NotFoundException("Delivery location not found: " + req.deliveryLocationId()));
        transport.setPickupLocation(pickup);
        transport.setDeliveryLocation(delivery);

        //driver not assigned here on purpose
        //assigning driver via assignDriver
        return transportMapper.toDto(transport);
    }

    // --- history helper ---

    private void saveHistory(Transport transport, TransportStatus status, Long userId) {
        User changer = null;
        if (userId != null) {
            changer = userRepository.findById(userId).orElse(null);
        }
        saveHistory(transport, status, changer);
    }

    private void saveHistory(Transport transport, TransportStatus status, User changer) {
        StatusHistory history = StatusHistory.builder()
                .transport(transport)
                .status(status)
                .changedBy(changer)
                .build();

        statusHistoryRepository.save(history);
    }



    // Returns timeline entries for all transports of a driver.
    public List<StatusHistoryDTO> getTimelineForDriver(Long driverId) {

        var transports = transportRepository.findByDriver(driverId);

        if (transports.isEmpty())
            return List.of();

        return transports.stream()
                .flatMap(t -> statusHistoryRepository.findByTransport_IdOrderByChangedAtDesc(t.getId()).stream())
                .sorted((a, b) -> a.getChangedAt().compareTo(b.getChangedAt()))
                .map(statusHistoryMapper::toDto)
                .toList();
    }

    // Searches transports using optional filters and pagination.
    public Page<TransportDTO> search(
            TransportStatus status,
            Long driverId,
            Long vehicleId,
            Instant from,
            Instant to,
            String q,
            Pageable pageable
    ) {
        var spec = TransportSpecifications.withFilters(status, driverId, vehicleId, from, to, q);

        return transportRepository.findAll(spec, pageable)
                .map(transportMapper::toDto);
    }


    private void validateVehicleAvailable(Long vehicleId) {
        if (vehicleId == null) return;

        var vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found: " + vehicleId));

        if (vehicle.getVehicleStatus() != com.damocles.fleet.fleetmanagementsystembackend.domain.VehicleStatus.ACTIVE) {
            throw new BusinessValidationException("Vehicle is not ACTIVE");
        }

        if (transportRepository.existsActiveTransportByVehicle(vehicleId)) {
            throw new BusinessValidationException("Vehicle is already assigned to an active transport");
        }
    }

    private void validateTrailerAvailable(Long trailerId) {
        if (trailerId == null) return;

        var trailer = trailerRepository.findById(trailerId)
                .orElseThrow(() -> new NotFoundException("Trailer not found: " + trailerId));

        if (trailer.getTrailerStatus() != com.damocles.fleet.fleetmanagementsystembackend.domain.TrailerStatus.ACTIVE) {
            throw new BusinessValidationException("Trailer is not ACTIVE");
        }

        if (transportRepository.existsActiveTransportByTrailer(trailerId)) {
            throw new BusinessValidationException("Trailer is already assigned to an active transport");
        }
    }

    private void validateDriverAvailable(Long driverId) {
        if (driverId == null) return;

        var driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new NotFoundException("Driver not found: " + driverId));

        if (driver.getDriverStatus() != com.damocles.fleet.fleetmanagementsystembackend.domain.DriverStatus.AVAILABLE) {
            throw new BusinessValidationException("Driver is not AVAILABLE");
        }

        if (transportRepository.existsActiveTransportByDriver(driverId)) {
            throw new BusinessValidationException("Driver already has an active transport");
        }
    }

    private void validatePlannedDates(CreateTransportRequest req) {
        if (req == null) return;

        Instant start = req.plannedStartAt();
        Instant end = req.plannedEndAt();
        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessValidationException("Planned end must be after planned start");
        }
    }
}
