package com.damocles.fleet.fleetmanagementsystembackend.service;

import com.damocles.fleet.fleetmanagementsystembackend.dto.statusHistory.StatusHistoryDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.CreateTransportRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.TransportDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ITransportService {
    List<TransportDTO> getAllTransports();
    TransportDTO getTransportById(Long id);
    TransportDTO createTransport(CreateTransportRequest req, Long id);
    TransportDTO updateTransport(Long id, CreateTransportRequest req, Authentication auth);
    TransportDTO assignDriver(Long transportId, Long driverId);
    List<TransportDTO> getTransportsForDriver(Long driverId);
    void deleteTransport(Long id);
    List<StatusHistoryDTO> getStatusHistories(Long transportId);

    TransportDTO adminUpdateTransport(Long id, CreateTransportRequest req);
}