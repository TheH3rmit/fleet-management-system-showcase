package com.damocles.fleet.fleetmanagementsystembackend.web;

import com.damocles.fleet.fleetmanagementsystembackend.dto.cargo.CargoDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driver.CreateDriverRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driver.DriverDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driver.UpdateDriverRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driver.UpdateDriverStatusRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.statusHistory.StatusHistoryDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.TransportDTO;
import com.damocles.fleet.fleetmanagementsystembackend.service.CargoService;
import com.damocles.fleet.fleetmanagementsystembackend.service.DriverService;
import com.damocles.fleet.fleetmanagementsystembackend.service.TransportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;
    private final CargoService cargoService;
    private final TransportService transportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    // List drivers with optional search and pagination.
    public Page<DriverDTO> listDrivers(
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        return driverService.searchDrivers(q, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    // Get driver by id.
    public DriverDTO getDriverById(@PathVariable Long id) {
        return driverService.getDriverById(id);
    }

    @GetMapping(params = "ids")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Bulk fetch drivers by ids.
    public List<DriverDTO> getByIds(@RequestParam List<Long> ids) {
        return driverService.getDriversByIds(ids);
    }


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    // Create a driver for a user.
    public ResponseEntity<DriverDTO> createDriver(@Valid @RequestBody CreateDriverRequest req) {
        return ResponseEntity.ok(driverService.createDriver(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    // Update driver details.
    public DriverDTO updateDriver(@PathVariable Long id, @Valid @RequestBody UpdateDriverRequest req) {
        return driverService.updateDriver(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    // Delete driver by user id.
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // List available drivers.
    public List<DriverDTO> available() {
        return driverService.getAvailableDrivers();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    // Update driver status.
    public ResponseEntity<DriverDTO> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDriverStatusRequest req
    ) {
        return ResponseEntity.ok(driverService.updateStatus(id, req.status()));
    }

    @GetMapping("/my-cargo")
    @PreAuthorize("hasRole('DRIVER')")
    // Driver view: cargo assigned to current driver.
    public List<CargoDTO> myCargo(Authentication auth) {
        Long driverId = transportService.getDriverIdByAccountLogin(auth.getName());
        return cargoService.getCargosForDriver(driverId);
    }

    @GetMapping("/my-transports/timeline")
    @PreAuthorize("hasRole('DRIVER')")
    // Driver view: timeline for current driver's transports.
    public List<StatusHistoryDTO> myTimeline(Authentication auth) {
        Long driverId = transportService.getDriverIdByAccountLogin(auth.getName());
        return transportService.getTimelineForDriver(driverId);
    }

    //Show me my transports
    //This is not logically duplicated endpoint it serves different view
    // but the underlying logic is the same and is dependent on user role
    //TransportController.byDriver
    @GetMapping("/my-transports")
    @PreAuthorize("hasRole('DRIVER')")
    // Driver view: list transports assigned to current driver.
    public List<TransportDTO> myTransports(Authentication auth) {
        String username = auth.getName(); // account logic
        Long driverId = transportService.getDriverIdByAccountLogin(username);
        return transportService.getTransportsForDriver(driverId);
    }
}
