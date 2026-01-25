package com.damocles.fleet.fleetmanagementsystembackend.web;

import com.damocles.fleet.fleetmanagementsystembackend.dto.driverWorkLog.CreateDriverWorkLogRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driverWorkLog.DriverWorkLogDTO;
import com.damocles.fleet.fleetmanagementsystembackend.exception.ForbiddenException;
import com.damocles.fleet.fleetmanagementsystembackend.service.DriverWorkLogService;
import com.damocles.fleet.fleetmanagementsystembackend.service.TransportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-logs")
@RequiredArgsConstructor
public class DriverWorkLogController {

    private final DriverWorkLogService service;
    private final TransportService transportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // List all work log entries (admin/dispatcher).
    public List<DriverWorkLogDTO> getAll() {
        return service.getAllLogs();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','DRIVER')")
    // Get a work log entry by id (driver restricted to own entries).
    public DriverWorkLogDTO getById(@PathVariable Long id, Authentication auth) {
        DriverWorkLogDTO dto = service.getLogById(id);
        if (hasRole(auth, "DRIVER")) {
            Long driverId = currentDriverId(auth);
            if (!driverId.equals(dto.driverId())) {
                throw new ForbiddenException("Drivers can only access their own work logs");
            }
        }
        return dto;
    }

    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','DRIVER')")
    // List work logs for a driver (driver restricted to own entries).
    public List<DriverWorkLogDTO> getByDriver(@PathVariable Long driverId, Authentication auth) {
        if (hasRole(auth, "DRIVER")) {
            Long currentId = currentDriverId(auth);
            if (!currentId.equals(driverId)) {
                throw new ForbiddenException("Drivers can only access their own work logs");
            }
        }
        return service.getLogsByDriver(driverId);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('DRIVER')")
    // List work logs for current driver.
    public List<DriverWorkLogDTO> getMy(Authentication auth) {
        Long driverId = currentDriverId(auth);
        return service.getLogsByDriver(driverId);
    }

    @GetMapping("/transport/{transportId}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // List work logs for a transport.
    public List<DriverWorkLogDTO> getByTransport(@PathVariable Long transportId) {
        return service.getLogsByTransport(transportId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    // Create a work log entry (admin).
    public ResponseEntity<DriverWorkLogDTO> create(@Valid @RequestBody CreateDriverWorkLogRequest req) {
        return ResponseEntity.ok(service.createLog(req));
    }

    @PostMapping("/my")
    @PreAuthorize("hasRole('DRIVER')")
    // Create a work log entry for current driver.
    public ResponseEntity<DriverWorkLogDTO> createMy(
            @Valid @RequestBody CreateDriverWorkLogRequest req,
            Authentication auth
    ) {
        Long driverId = currentDriverId(auth);
        CreateDriverWorkLogRequest normalized = new CreateDriverWorkLogRequest(
                req.startTime(),
                req.endTime(),
                req.breakDuration(),
                req.notes(),
                driverId,
                req.transportId(),
                req.activityType()
        );
        return ResponseEntity.ok(service.createLog(normalized));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    // Update a work log entry (admin).
    public DriverWorkLogDTO update(@PathVariable Long id, @Valid @RequestBody CreateDriverWorkLogRequest req) {
        return service.updateLog(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    // Delete a work log entry (admin).
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteLog(id);
        return ResponseEntity.noContent().build();
    }

    private Long currentDriverId(Authentication auth) {
        return transportService.getDriverIdByAccountLogin(auth.getName());
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
