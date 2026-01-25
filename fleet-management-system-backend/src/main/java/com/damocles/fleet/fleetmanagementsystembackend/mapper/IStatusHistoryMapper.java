package com.damocles.fleet.fleetmanagementsystembackend.mapper;

import com.damocles.fleet.fleetmanagementsystembackend.domain.StatusHistory;
import com.damocles.fleet.fleetmanagementsystembackend.dto.statusHistory.StatusHistoryDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IStatusHistoryMapper {

    @Mapping(target = "transportId", source = "transport.id")
    @Mapping(target = "changedById", source = "changedBy.id")
    @Mapping(
            target = "changedByName",
            expression = "java(h.getChangedBy() == null ? null : (h.getChangedBy().getFirstName() + \" \" + h.getChangedBy().getLastName()))"
    )
    StatusHistoryDTO toDto(StatusHistory h);
}