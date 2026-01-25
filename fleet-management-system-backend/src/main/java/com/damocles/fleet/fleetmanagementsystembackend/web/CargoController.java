package com.damocles.fleet.fleetmanagementsystembackend.web;


import com.damocles.fleet.fleetmanagementsystembackend.dto.cargo.CargoDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.cargo.CreateCargoForTransportRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.cargo.CreateCargoRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.cargo.UpdateCargoRequest;
import com.damocles.fleet.fleetmanagementsystembackend.service.CargoService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cargos")
@RequiredArgsConstructor
public class CargoController {

    private final CargoService cargoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // List cargos with optional search and pagination.
    public Page<CargoDTO> list(
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        return cargoService.searchCargos(q, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Get cargo by id.
    public CargoDTO getById(@PathVariable Long id) {
        return cargoService.getCargoById(id);
    }

    @GetMapping("/transport/{transportId}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // List cargos for a transport.
    public List<CargoDTO> getByTransport(@PathVariable Long transportId) {
        return cargoService.getCargosByTransport(transportId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Create cargo.
    public ResponseEntity<CargoDTO> create(@Valid @RequestBody CreateCargoRequest req) {
        return ResponseEntity.ok(cargoService.createCargo(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Update cargo by id.
    public CargoDTO update(@PathVariable Long id, @Valid @RequestBody UpdateCargoRequest req) {
        return cargoService.updateCargo(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Delete cargo by id.
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cargoService.deleteCargo(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transport/{transportId}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Create cargo for a specific transport.
    public ResponseEntity<CargoDTO> createForTransport(
            @PathVariable Long transportId,
            @Valid @RequestBody CreateCargoForTransportRequest req
    ) {
        var fixed = new CreateCargoRequest(
                req.cargoDescription(),
                req.weightKg(),
                req.volumeM3(),
                req.pickupDate(),
                req.deliveryDate(),
                transportId
        );
        return ResponseEntity.ok(cargoService.createCargo(fixed));
    }
}
