package com.damocles.fleet.fleetmanagementsystembackend.service;

import com.damocles.fleet.fleetmanagementsystembackend.dto.driver.CreateDriverRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driver.DriverDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driver.UpdateDriverRequest;

import java.util.List;

public interface IDriverService {
    List<DriverDTO> getAllDrivers();
    DriverDTO getDriverById(Long id);
    DriverDTO createDriver(CreateDriverRequest req);
    DriverDTO updateDriver(Long id, UpdateDriverRequest req);
    List<DriverDTO> getAvailableDrivers();
    void deleteDriver(Long id);
}