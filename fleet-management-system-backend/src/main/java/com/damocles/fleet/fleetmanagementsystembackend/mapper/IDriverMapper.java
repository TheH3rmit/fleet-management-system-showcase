package com.damocles.fleet.fleetmanagementsystembackend.mapper;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Driver;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driver.CreateDriverRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driver.DriverDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driver.UpdateDriverRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IDriverMapper {

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "hasTransports", ignore = true)
    @Mapping(target = "hasWorkLogs", ignore = true)
    DriverDTO toDto(Driver driver);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "user", ignore = true)
    Driver toEntity(CreateDriverRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDriverFromDto(UpdateDriverRequest req, @MappingTarget Driver driver);
}
