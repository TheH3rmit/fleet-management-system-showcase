package com.damocles.fleet.fleetmanagementsystembackend.web;

import com.damocles.fleet.fleetmanagementsystembackend.dto.statusHistory.StatusHistoryDTO;
import com.damocles.fleet.fleetmanagementsystembackend.service.TransportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transports")
@RequiredArgsConstructor
public class StatusHistoryController {

    private final TransportService transportService;

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    // List status history for a transport.
    public List<StatusHistoryDTO> history(@PathVariable Long id) {
        return transportService.getStatusHistories(id);
    }
}
