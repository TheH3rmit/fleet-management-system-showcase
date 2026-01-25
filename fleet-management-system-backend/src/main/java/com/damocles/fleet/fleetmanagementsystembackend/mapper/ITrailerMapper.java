package com.damocles.fleet.fleetmanagementsystembackend.mapper;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Trailer;
import com.damocles.fleet.fleetmanagementsystembackend.dto.trailer.CreateTrailerRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.trailer.TrailerDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ITrailerMapper {

    @Mapping(target = "assignedToTransport", ignore = true)
    @Mapping(target = "inProgressAssigned", ignore = true)
    TrailerDTO toDto(Trailer trailer);

    Trailer toEntity(CreateTrailerRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTrailerFromDto(CreateTrailerRequest req, @MappingTarget Trailer trailer);
}
