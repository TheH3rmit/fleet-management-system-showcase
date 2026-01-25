package com.damocles.fleet.fleetmanagementsystembackend.service;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Driver;
import com.damocles.fleet.fleetmanagementsystembackend.domain.DriverStatus;
import com.damocles.fleet.fleetmanagementsystembackend.domain.User;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driver.CreateDriverRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driver.DriverDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driver.UpdateDriverRequest;
import com.damocles.fleet.fleetmanagementsystembackend.exception.BusinessValidationException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.ConflictException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.DriverNotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.NotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.IDriverMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IDriverWorkLogRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IDriverRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITransportRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class DriverService implements IDriverService {

    private final IDriverRepository driverRepository;
    private final IUserRepository userRepository;
    private final IDriverMapper driverMapper;
    private final ITransportRepository transportRepository;
    private final IDriverWorkLogRepository driverWorkLogRepository;

    // Resolves a list of drivers by id with a defensive size limit.
    public List<DriverDTO> getDriversByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();

        if (ids.size() > 200) {
            throw new BusinessValidationException("Too many ids (max 200)");
        }

        return driverRepository.findAllById(ids).stream()
                .map(this::enrichDriver)
                .toList();
    }

    @Override
    // Returns all drivers without filtering.
    public List<DriverDTO> getAllDrivers() {
        return driverRepository.findAll()
                .stream()
                .map(this::enrichDriver)
                .toList();
    }

    @Override
    // Fetches a single driver by id.
    public DriverDTO getDriverById(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new DriverNotFoundException(id));
        return enrichDriver(driver);
    }

    @Override
    // Creates a driver profile linked to an existing user.
    public DriverDTO createDriver(CreateDriverRequest req) {
        if (driverRepository.existsByDriverLicenseNumber(req.driverLicenseNumber())) {
            throw new ConflictException("Driver with license number " + req.driverLicenseNumber() + " already exists");
        }

        if (driverRepository.existsByUserId(req.userId())) {
            throw new ConflictException("Driver for userId " + req.userId() + " already exists");
        }

        User user = userRepository.findById(req.userId())
                .orElseThrow(() -> new DriverNotFoundException("User with ID " + req.userId() + " not found"));

        Driver driver = driverMapper.toEntity(req);
        driver.setUser(user);

        driverRepository.save(driver);
        return enrichDriver(driver);
    }

    @Override
    // Updates driver fields with license number uniqueness checks.
    public DriverDTO updateDriver(Long userId, UpdateDriverRequest req) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Driver not found for userId: " + userId));

        // validation: uniqueness of license number on change
        if (req.driverLicenseNumber() != null && !req.driverLicenseNumber().isBlank()) {
            String newLic = req.driverLicenseNumber().trim();
            String oldLic = driver.getDriverLicenseNumber();
            if (!newLic.equals(oldLic) && driverRepository.existsByDriverLicenseNumber(newLic)) {
                throw new ConflictException("Driver with license number " + newLic + " already exists");
            }
        }

        driverMapper.updateDriverFromDto(req, driver);
        return enrichDriver(driver);
    }

    @Override
    // Deletes a driver only if no transports or work logs are linked.
    public void deleteDriver(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new DriverNotFoundException(id));
        if (transportRepository.existsByDriver_UserId(driver.getUserId())) {
            throw new BusinessValidationException("Driver has transports and cannot be deleted");
        }
        if (driverWorkLogRepository.existsByDriver_UserId(driver.getUserId())) {
            throw new BusinessValidationException("Driver has work logs and cannot be deleted");
        }
        driverRepository.delete(driver);
    }

    // Returns drivers that are currently available.
    public List<DriverDTO> getAvailableDrivers() {
        return driverRepository.findAvailable()
                .stream().map(driverMapper::toDto).toList();
    }

    // Searches drivers by a free-text query with paging.
    public Page<DriverDTO> searchDrivers(String q, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim().toLowerCase();

        if (query == null) {
            return driverRepository.findAll(pageable)
                    .map(this::enrichDriver);
        }

        return driverRepository.search(query, pageable)
                .map(this::enrichDriver);
    }

    @Transactional
    // Updates driver status with active transport guardrails.
    public DriverDTO updateStatus(Long userId, DriverStatus status) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Driver not found"));

        // Rule: driver cannot be AVAILABLE if he has active transport
        if (status == DriverStatus.AVAILABLE &&
                transportRepository.existsActiveTransportByDriver(userId)) {
            throw new BusinessValidationException("Driver is assigned to active transport");
        }

        driver.setDriverStatus(status);
        return enrichDriver(driver);
    }

    private DriverDTO enrichDriver(Driver driver) {
        DriverDTO base = driverMapper.toDto(driver);
        boolean hasTransports = transportRepository.existsByDriver_UserId(driver.getUserId());
        boolean hasWorkLogs = driverWorkLogRepository.existsByDriver_UserId(driver.getUserId());
        return new DriverDTO(
                base.userId(),
                base.firstName(),
                base.lastName(),
                base.email(),
                base.phone(),
                base.driverLicenseNumber(),
                base.driverLicenseCategory(),
                base.driverLicenseExpiryDate(),
                base.driverStatus(),
                hasTransports,
                hasWorkLogs
        );
    }
}
