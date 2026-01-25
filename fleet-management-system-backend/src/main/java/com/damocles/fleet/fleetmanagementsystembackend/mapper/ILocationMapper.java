package com.damocles.fleet.fleetmanagementsystembackend.mapper;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Location;
import com.damocles.fleet.fleetmanagementsystembackend.dto.location.CreateLocationRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.location.LocationDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ILocationMapper {

    @Mapping(target = "usedAsPickup", ignore = true)
    @Mapping(target = "usedAsDelivery", ignore = true)
    @Mapping(target = "usedInTransport", ignore = true)
    LocationDTO toDto(Location location);

    Location toEntity(CreateLocationRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CreateLocationRequest req, @MappingTarget Location location);
}
