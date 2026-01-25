package com.damocles.fleet.fleetmanagementsystembackend.mapper;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Vehicle;
import com.damocles.fleet.fleetmanagementsystembackend.dto.vehicle.CreateVehicleRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.vehicle.VehicleDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IVehicleMapper {

    @Mapping(target = "assignedToTransport", ignore = true)
    @Mapping(target = "inProgressAssigned", ignore = true)
    VehicleDTO toDto(Vehicle entity);

    Vehicle toEntity(CreateVehicleRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateVehicleFromDto(CreateVehicleRequest dto, @MappingTarget Vehicle entity);
}
