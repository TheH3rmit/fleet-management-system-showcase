package com.damocles.fleet.fleetmanagementsystembackend.mapper;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Cargo;
import com.damocles.fleet.fleetmanagementsystembackend.dto.cargo.CargoDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.cargo.CreateCargoRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ICargoMapper {

    @Mapping(target = "transportId", source = "transport.id")
    @Mapping(target = "transportStatus", source = "transport.status")
    CargoDTO toDto(Cargo cargo);

    Cargo toEntity(CreateCargoRequest req);
}
