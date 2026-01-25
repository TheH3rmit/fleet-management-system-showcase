package com.damocles.fleet.fleetmanagementsystembackend.mapper;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Account;
import com.damocles.fleet.fleetmanagementsystembackend.dto.view.AccountUserViewDTO;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = { IAccountMapper.class, IUserMapper.class },
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IAccountUserViewMapper {

    @Mappings({
            @Mapping(target = "account", source = "."),

            @Mapping(target = "user", source = "user")
    })
    AccountUserViewDTO toView(Account account);
}
