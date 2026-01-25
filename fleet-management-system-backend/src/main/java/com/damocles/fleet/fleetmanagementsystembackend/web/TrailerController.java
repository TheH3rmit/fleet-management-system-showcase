package com.damocles.fleet.fleetmanagementsystembackend.web;

import com.damocles.fleet.fleetmanagementsystembackend.dto.trailer.CreateTrailerRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.trailer.UpdateTrailerStatusRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.trailer.TrailerDTO;
import com.damocles.fleet.fleetmanagementsystembackend.service.TrailerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trailers")
@RequiredArgsConstructor
public class TrailerController {

    private final TrailerService trailerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // List trailers with optional search and pagination.
    public Page<TrailerDTO> listTrailers(
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        return trailerService.searchTrailers(q, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Get trailer by id.
    public TrailerDTO getById(@PathVariable Long id) {
        return trailerService.getTrailerById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Create a trailer.
    public ResponseEntity<TrailerDTO> create(@Valid @RequestBody CreateTrailerRequest req) {
        return ResponseEntity.ok(trailerService.createTrailer(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Update a trailer.
    public TrailerDTO update(@PathVariable Long id, @Valid @RequestBody CreateTrailerRequest req) {
        return trailerService.updateTrailer(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // Delete a trailer.
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        trailerService.deleteTrailer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // List available trailers.
    public List<TrailerDTO> getAvailable() {
        return trailerService.getAvailableTrailers();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    // Update trailer status.
    public TrailerDTO updateTrailerStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTrailerStatusRequest req
    ) {
        return trailerService.updateStatus(id, req.status());
    }

}
