package com.damocles.fleet.fleetmanagementsystembackend.service;
import com.damocles.fleet.fleetmanagementsystembackend.dto.location.CreateLocationRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.location.LocationDTO;

import java.util.List;
public interface ILocationService {
    List<LocationDTO> getAllLocations();
    LocationDTO getLocationById(Long id);
    LocationDTO createLocation(CreateLocationRequest req);
    LocationDTO updateLocation(Long id, CreateLocationRequest req);
    public List<LocationDTO> search(String location);
    void deleteLocation(Long id);
}