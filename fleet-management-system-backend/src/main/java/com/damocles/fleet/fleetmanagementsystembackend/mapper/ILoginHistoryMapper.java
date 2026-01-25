package com.damocles.fleet.fleetmanagementsystembackend.mapper;
import com.damocles.fleet.fleetmanagementsystembackend.domain.LoginHistory;
import com.damocles.fleet.fleetmanagementsystembackend.dto.loginHistory.LoginHistoryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ILoginHistoryMapper {

    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "accountLogin", source = "account.login")
    LoginHistoryDTO toDto(LoginHistory history);

}
