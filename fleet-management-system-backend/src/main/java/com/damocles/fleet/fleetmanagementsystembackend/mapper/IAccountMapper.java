package com.damocles.fleet.fleetmanagementsystembackend.mapper;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Account;
import com.damocles.fleet.fleetmanagementsystembackend.dto.account.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IAccountMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "passwordHash", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "lastLoginAt", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "user", ignore = true)
    })
    Account toEntity(AccountRegisterDTO dto);

    // read
    @Mappings({
            @Mapping(target = "userId", source = "user.id"),
            @Mapping(target = "roles", source = "roles")
    })
    AccountResponseDTO toResponse(Account entity);

    // admin change status
    // UPDATE STATUS ONLY
    @Mapping(target = "status", source = "dto.status")
    void updateStatus(@MappingTarget Account entity, AccountStatusUpdateDTO dto);

}
