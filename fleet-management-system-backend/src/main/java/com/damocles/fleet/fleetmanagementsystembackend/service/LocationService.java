package com.damocles.fleet.fleetmanagementsystembackend.service;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Location;
import com.damocles.fleet.fleetmanagementsystembackend.dto.location.CreateLocationRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.location.LocationDTO;
import com.damocles.fleet.fleetmanagementsystembackend.exception.ConflictException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.LocationNotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.ILocationMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ILocationRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITransportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationService implements ILocationService {

    private final ILocationRepository locationRepository;
    private final ILocationMapper locationMapper;
    private final ITransportRepository transportRepository;

    @Override
    // Returns all locations with usage flags.
    public List<LocationDTO> getAllLocations() {
        return locationRepository.findAll()
                .stream()
                .map(this::enrichLocation)
                .toList();
    }

    // Searches locations by free-text query with paging.
    public Page<LocationDTO> searchLocations(String q, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim().toLowerCase();

        if (query == null) {
            return locationRepository.findAll(pageable)
                    .map(this::enrichLocation);
        }

        return locationRepository.searchPage(query, pageable)
                .map(this::enrichLocation);
    }

    @Override
    // Fetches a single location by id.
    public LocationDTO getLocationById(Long id) {
        Location loc = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));
        return enrichLocation(loc);
    }

    @Override
    // Creates a new location with uniqueness checks.
    public LocationDTO createLocation(CreateLocationRequest req) {
        if (locationRepository.existsByStreetAndBuildingNumberAndCity(req.street(), req.buildingNumber(), req.city())) {
            throw new ConflictException("Location already exists at " + req.street() + " " + req.buildingNumber() + ", " + req.city());
        }
        Location loc = locationMapper.toEntity(req);
        locationRepository.save(loc);
        return enrichLocation(loc);
    }

    @Override
    // Updates an existing location with address conflict checks.
    public LocationDTO updateLocation(Long id, CreateLocationRequest req) {
        Location loc = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));

        boolean addressChanged =
                !safeEq(loc.getStreet(), req.street()) ||
                        !safeEq(loc.getBuildingNumber(), req.buildingNumber()) ||
                        !safeEq(loc.getCity(), req.city());

        if (addressChanged &&
                locationRepository.existsByStreetAndBuildingNumberAndCity(req.street(), req.buildingNumber(), req.city())) {
            throw new ConflictException("Location already exists at " + req.street() + " " + req.buildingNumber() + ", " + req.city());
        }

        locationMapper.updateFromDto(req, loc);
        return enrichLocation(loc);
    }

    private boolean safeEq(String a, String b) {
        return (a == null ? "" : a.trim()).equalsIgnoreCase(b == null ? "" : b.trim());
    }

    @Override
    // Deletes a location only if it is not used by transports.
    public void deleteLocation(Long id) {
        Location loc = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));
        boolean usedAsPickup = transportRepository.existsByPickupLocation_Id(id);
        boolean usedAsDelivery = transportRepository.existsByDeliveryLocation_Id(id);

        if (usedAsPickup && usedAsDelivery) {
            throw new ConflictException("Location is used as pickup and delivery in transports and cannot be deleted");
        }
        if (usedAsPickup) {
            throw new ConflictException("Location is used as pickup in transports and cannot be deleted");
        }
        if (usedAsDelivery) {
            throw new ConflictException("Location is used as delivery in transports and cannot be deleted");
        }
        locationRepository.delete(loc);
    }

    @Override
    // Searches locations without pagination for quick lookup lists.
    public List<LocationDTO> search(String location) {
        return locationRepository.search(location).stream()
                .map(this::enrichLocation)
                .toList();
    }

    private LocationDTO enrichLocation(Location loc) {
        LocationDTO base = locationMapper.toDto(loc);
        boolean usedAsPickup = transportRepository.existsByPickupLocation_Id(loc.getId());
        boolean usedAsDelivery = transportRepository.existsByDeliveryLocation_Id(loc.getId());
        boolean usedInTransport = usedAsPickup || usedAsDelivery;
        return new LocationDTO(
                base.id(),
                base.street(),
                base.buildingNumber(),
                base.city(),
                base.postcode(),
                base.country(),
                base.latitude(),
                base.longitude(),
                usedAsPickup,
                usedAsDelivery,
                usedInTransport
        );
    }
}
