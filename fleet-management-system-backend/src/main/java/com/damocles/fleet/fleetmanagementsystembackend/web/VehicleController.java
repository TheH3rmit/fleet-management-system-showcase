package com.damocles.fleet.fleetmanagementsystembackend.web;

import com.damocles.fleet.fleetmanagementsystembackend.dto.vehicle.CreateVehicleRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.vehicle.UpdateVehicleStatusRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.vehicle.VehicleDTO;
import com.damocles.fleet.fleetmanagementsystembackend.service.IVehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final IVehicleService vehicleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    // List vehicles with optional search and pagination.
    public Page<VehicleDTO> listVehicles(
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        return vehicleService.searchVehicles(q, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    // Get vehicle by id.
    public VehicleDTO getVehicleById(@PathVariable Long id) {
        return vehicleService.getVehicleById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    // Create a vehicle.
    public ResponseEntity<VehicleDTO> createVehicle(@Valid @RequestBody CreateVehicleRequest req) {
        return ResponseEntity.ok(vehicleService.createVehicle(req));
    }
    @GetMapping(params = "ids")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    // Bulk fetch vehicles by ids.
    public List<VehicleDTO> getByIds(@RequestParam List<Long> ids) {
        return vehicleService.getVehiclesByIds(ids);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    // Update a vehicle.
    public VehicleDTO updateVehicle(@PathVariable Long id, @Valid @RequestBody CreateVehicleRequest req) {
        return vehicleService.updateVehicle(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    // Delete a vehicle.
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // List available vehicles.
    public List<VehicleDTO> available() {
        return vehicleService.getAvailableVehicles();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    // Update vehicle status.
    public VehicleDTO updateVehicleStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleStatusRequest req
    ) {
        return vehicleService.updateStatus(id, req.status());
    }

}
