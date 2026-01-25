package com.damocles.fleet.fleetmanagementsystembackend.service;

import com.damocles.fleet.fleetmanagementsystembackend.dto.cargo.CreateCargoRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.cargo.CargoDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.cargo.UpdateCargoRequest;

import java.util.List;

public interface ICargoService {
    List<CargoDTO> getAllCargos();
    List<CargoDTO> getCargosByTransport(Long transportId);
    CargoDTO getCargoById(Long id);
    CargoDTO createCargo(CreateCargoRequest req);
    CargoDTO updateCargo(Long id, UpdateCargoRequest req);
    void deleteCargo(Long id);
}