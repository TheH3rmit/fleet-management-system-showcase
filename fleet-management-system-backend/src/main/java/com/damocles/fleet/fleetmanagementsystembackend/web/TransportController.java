package com.damocles.fleet.fleetmanagementsystembackend.web;
import com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.CreateTransportRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.TransportDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.TransportDetailsDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.UpdateTransportStatusRequest;
import com.damocles.fleet.fleetmanagementsystembackend.service.TransportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/transports")
@RequiredArgsConstructor
public class TransportController  {

    private final TransportService transportService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Get transport by id.
    public TransportDTO getById(@PathVariable Long id) {
        return transportService.getTransportById(id);
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','DRIVER')")
    // Get transport details by id.
    public TransportDetailsDTO getDetails(@PathVariable Long id) {
        return transportService.getTransportDetails(id);
    }
    @PutMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    // Admin-only update for transport.
    public TransportDTO adminUpdate(@PathVariable Long id, @Valid @RequestBody CreateTransportRequest req) {
        return transportService.adminUpdateTransport(id, req);
    }

    // ADMIN/DISPATCHER - create
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Create a transport (admin/dispatcher).
    public TransportDTO create(@Valid @RequestBody CreateTransportRequest req, Authentication auth) {
        Long userId = transportService.getUserIdByAccountLogin(auth.getName());
        return transportService.createTransport(req, userId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Update a transport (admin/dispatcher).
    public TransportDTO update(@PathVariable Long id,
                               @Valid @RequestBody CreateTransportRequest req,
                               Authentication auth) {
        return transportService.updateTransport(id, req, auth);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Delete a transport (admin/dispatcher).
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transportService.deleteTransport(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/assign-driver/{driverId}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Assign driver to transport.
    public TransportDTO assignDriver(@PathVariable Long id, @PathVariable Long driverId) {
        return transportService.assignDriver(id, driverId);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','DRIVER')")
    // Change transport status for admin/dispatcher/driver.
    public TransportDTO updateStatusPatch(
            @PathVariable Long id,
            Authentication auth,
            @Valid @RequestBody UpdateTransportStatusRequest req
    ) {
        return changeStatusForAuth(id, req.status(), auth);
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Search transports by filters and pagination.
    public Page<TransportDTO> listTransports(
            @RequestParam(required = false) TransportStatus status,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable
    ) {
        return transportService.search(status, driverId, vehicleId, from, to, q, pageable);
    }
    //Show me transports of selected driver
    //This is not logically duplicated endpoint it serves different view
    // but the underlying logic is the same and is dependent on user role
    // DriverController.myTransports
    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // List transports for a specific driver.
    public List<TransportDTO> byDriver(@PathVariable Long driverId) {
        return transportService.getTransportsForDriver(driverId);
    }

    private TransportDTO changeStatusForAuth(Long id, TransportStatus status, Authentication auth) {
        if (hasRole(auth, "ROLE_DRIVER")) {
            Long driverId = transportService.getDriverIdByAccountLogin(auth.getName());
            return transportService.updateStatus(id, driverId, status);
        }

        Long userId = transportService.getUserIdByAccountLogin(auth.getName());
        return transportService.changeStatusByAdmin(id, status, userId);
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}
