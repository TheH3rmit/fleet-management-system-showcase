package com.damocles.fleet.fleetmanagementsystembackend.mapper;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Transport;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.CreateTransportRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.TransportDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.TransportDetailsDTO;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = { ILocationMapper.class },
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface ITransportMapper {

    // ENTITY -> DTO (list/standard)
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "trailerId", source = "trailer.id")
    @Mapping(target = "trailerLabel",
            expression = "java(transport.getTrailer() == null ? null : (transport.getTrailer().getLicensePlate() + \" - \" + transport.getTrailer().getName()))")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleLabel",
            expression = "java(transport.getVehicle() == null ? null : (transport.getVehicle().getLicensePlate() + \" - \" + transport.getVehicle().getManufacturer() + \" \" + transport.getVehicle().getModel()))")
    @Mapping(target = "pickupLocationId", source = "pickupLocation.id")
    @Mapping(target = "deliveryLocationId", source = "deliveryLocation.id")
    @Mapping(target = "driverId", source = "driver.userId")
    TransportDTO toDto(Transport transport);

    // REQUEST -> ENTITY (create) (relation and status in service)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "trailer", ignore = true),
            @Mapping(target = "vehicle", ignore = true),
            @Mapping(target = "pickupLocation", ignore = true),
            @Mapping(target = "deliveryLocation", ignore = true),
            @Mapping(target = "driver", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "cargos", ignore = true),
            @Mapping(target = "statusHistories", ignore = true)
    })
    Transport toEntity(CreateTransportRequest req);

    // REQUEST -> ENTITY (update)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "trailer", ignore = true),
            @Mapping(target = "vehicle", ignore = true),
            @Mapping(target = "pickupLocation", ignore = true),
            @Mapping(target = "deliveryLocation", ignore = true),
            @Mapping(target = "driver", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "cargos", ignore = true),
            @Mapping(target = "statusHistories", ignore = true)
    })
    void updateFromDto(CreateTransportRequest req, @MappingTarget Transport transport);

    // ENTITY -> DETAILS DTO
    @Mappings({
            @Mapping(target = "createdById", source = "createdBy.id"),
            @Mapping(target = "createdByEmail", source = "createdBy.email"),
            @Mapping(target = "trailerId", source = "trailer.id"),
            @Mapping(target = "vehicleId", source = "vehicle.id"),
            @Mapping(target = "driverId", source = "driver.userId"),
            @Mapping(target = "pickupLocation", source = "pickupLocation"),
            @Mapping(target = "deliveryLocation", source = "deliveryLocation")
    })
    TransportDetailsDTO toDetailsDTO(Transport t);
}
