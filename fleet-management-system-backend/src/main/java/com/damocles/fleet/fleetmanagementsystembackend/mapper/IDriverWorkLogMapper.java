package com.damocles.fleet.fleetmanagementsystembackend.mapper;

import com.damocles.fleet.fleetmanagementsystembackend.domain.DriverWorkLog;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driverWorkLog.CreateDriverWorkLogRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driverWorkLog.DriverWorkLogDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IDriverWorkLogMapper {

    @Mapping(target = "driverId", source = "driver.userId")
    @Mapping(target = "driverName",
            expression = "java(log.getDriver() == null ? null : (log.getDriver().getUser().getFirstName() + \" \" + log.getDriver().getUser().getLastName()))")
    @Mapping(target = "transportId", source = "transport.id")
    DriverWorkLogDTO toDto(DriverWorkLog log);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "transport", ignore = true)
    DriverWorkLog toEntity(CreateDriverWorkLogRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "transport", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateFromDto(CreateDriverWorkLogRequest req, @MappingTarget DriverWorkLog log);
}
