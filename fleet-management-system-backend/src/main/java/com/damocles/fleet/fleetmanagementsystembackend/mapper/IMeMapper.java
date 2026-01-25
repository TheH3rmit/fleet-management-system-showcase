package com.damocles.fleet.fleetmanagementsystembackend.mapper;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Account;
import com.damocles.fleet.fleetmanagementsystembackend.dto.account.AccountUserShortDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IMeMapper {
    @Mapping(target = "accountId", source = "id")
    @Mapping(target = "login",     source = "login")
    @Mapping(target = "status",    source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "lastLoginAt", source = "lastLoginAt")

    @Mapping(target = "userId",    source = "user.id")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName",  source = "user.lastName")
    @Mapping(target = "email",     source = "user.email")
    AccountUserShortDTO toShort(Account account);
}
