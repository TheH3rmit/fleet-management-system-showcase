package com.damocles.fleet.fleetmanagementsystembackend.service;
import com.damocles.fleet.fleetmanagementsystembackend.domain.ActivityType;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Driver;
import com.damocles.fleet.fleetmanagementsystembackend.domain.DriverWorkLog;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Transport;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driverWorkLog.CreateDriverWorkLogRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driverWorkLog.DriverWorkLogDTO;
import com.damocles.fleet.fleetmanagementsystembackend.exception.*;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.IDriverWorkLogMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IDriverRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IDriverWorkLogRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITransportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverWorkLogService implements IDriverWorkLogService {

    private final IDriverWorkLogRepository driverWorkLogRepository;
    private final IDriverRepository driverRepository;
    private final ITransportRepository transportRepository;
    private final IDriverWorkLogMapper mapper;

    @Override
    // Returns all work log entries.
    public List<DriverWorkLogDTO> getAllLogs() {
        return driverWorkLogRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    // Fetches a single work log entry by id.
    public DriverWorkLogDTO getLogById(Long id) {
        DriverWorkLog log = driverWorkLogRepository.findById(id)
                .orElseThrow(() -> new DriverWorkLogNotFoundException(id));
        return mapper.toDto(log);
    }

    @Override
    // Lists work log entries for a driver, ordered by start time.
    public List<DriverWorkLogDTO> getLogsByDriver(Long driverId) {
        return driverWorkLogRepository.findByDriver_UserIdOrderByStartTimeDesc(driverId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    // Lists work log entries for a transport, ordered by start time.
    public List<DriverWorkLogDTO> getLogsByTransport(Long transportId) {
        return driverWorkLogRepository.findByTransport_IdOrderByStartTimeDesc(transportId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    // Creates a new work log entry with validation.
    public DriverWorkLogDTO createLog(CreateDriverWorkLogRequest req) {
        Driver driver = driverRepository.findById(req.driverId())
                .orElseThrow(() -> new NotFoundException("Driver not found: " + req.driverId()));
        Transport transport = transportRepository.findById(req.transportId())
                .orElseThrow(() -> new NotFoundException("Transport not found: " + req.transportId()));
        ActivityType type = req.activityType();

        validateDates(req.startTime(), req.endTime());
        validateBreakDuration(req.breakDuration());
        validateDriverMatchesTransport(driver, transport);

        DriverWorkLog log = mapper.toEntity(req);
        log.setDriver(driver);
        log.setTransport(transport);
        log.setActivityType(type);

        driverWorkLogRepository.save(log);
        return mapper.toDto(log);
    }

    @Override
    // Updates an existing work log entry with validation.
    public DriverWorkLogDTO updateLog(Long id, CreateDriverWorkLogRequest req) {
        DriverWorkLog log = driverWorkLogRepository.findById(id)
                .orElseThrow(() -> new DriverWorkLogNotFoundException(id));

        validateDates(req.startTime(), req.endTime());
        validateBreakDuration(req.breakDuration());

        Driver driver = driverRepository.findById(req.driverId())
                .orElseThrow(() -> new NotFoundException("Driver not found: " + req.driverId()));
        Transport transport = transportRepository.findById(req.transportId())
                .orElseThrow(() -> new NotFoundException("Transport not found: " + req.transportId()));
        ActivityType type = req.activityType();

        validateDriverMatchesTransport(driver, transport);

        mapper.updateFromDto(req, log);
        log.setDriver(driver);
        log.setTransport(transport);
        log.setActivityType(type);
        return mapper.toDto(log);
    }

    @Override
    // Deletes a work log entry by id.
    public void deleteLog(Long id) {
        DriverWorkLog log = driverWorkLogRepository.findById(id)
                .orElseThrow(() -> new DriverWorkLogNotFoundException(id));
        driverWorkLogRepository.delete(log);
    }

    private void validateDates(java.time.Instant start, java.time.Instant end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessValidationException("End time must be after start time");
        }
    }

    private void validateBreakDuration(Integer breakDuration) {
        if (breakDuration != null && breakDuration < 0) {
            throw new BusinessValidationException("Break duration must be >= 0");
        }
    }

    private void validateDriverMatchesTransport(Driver driver, Transport transport) {
        if (transport.getDriver() == null) {
            throw new BusinessValidationException("Transport has no driver assigned");
        }
        if (!transport.getDriver().getUserId().equals(driver.getUserId())) {
            throw new BusinessValidationException("Driver does not match transport assignment");
        }
    }
}
