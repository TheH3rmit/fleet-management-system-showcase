package com.damocles.fleet.fleetmanagementsystembackend.web;
import com.damocles.fleet.fleetmanagementsystembackend.dto.location.CreateLocationRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.location.LocationDTO;
import com.damocles.fleet.fleetmanagementsystembackend.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // List locations with optional search and pagination.
    public Page<LocationDTO> list(@RequestParam(required = false) String q, Pageable pageable) {
        return locationService.searchLocations(q, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Get location by id.
    public LocationDTO getById(@PathVariable Long id) {
        return locationService.getLocationById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Create a new location.
    public ResponseEntity<LocationDTO> create(@Valid @RequestBody CreateLocationRequest req) {
        return ResponseEntity.ok(locationService.createLocation(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Update a location.
    public LocationDTO update(@PathVariable Long id, @Valid @RequestBody CreateLocationRequest req) {
        return locationService.updateLocation(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Delete a location.
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

}
