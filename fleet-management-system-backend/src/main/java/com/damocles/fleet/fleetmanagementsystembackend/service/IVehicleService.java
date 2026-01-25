package com.damocles.fleet.fleetmanagementsystembackend.service;

import com.damocles.fleet.fleetmanagementsystembackend.domain.VehicleStatus;
import com.damocles.fleet.fleetmanagementsystembackend.dto.vehicle.CreateVehicleRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.vehicle.VehicleDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IVehicleService {

    List<VehicleDTO> getAllVehicles();
    Page<VehicleDTO> searchVehicles(String q, Pageable pageable);
    VehicleDTO getVehicleById(Long id);
    VehicleDTO createVehicle(CreateVehicleRequest req);
    VehicleDTO updateVehicle(Long id, CreateVehicleRequest req);
    List<VehicleDTO> getAvailableVehicles();
    void deleteVehicle(Long id);
    VehicleDTO updateStatus(Long id, VehicleStatus status);

    List<VehicleDTO> getVehiclesByIds(List<Long> ids);
}
