package com.damocles.fleet.fleetmanagementsystembackend.mapper;

import com.damocles.fleet.fleetmanagementsystembackend.domain.User;
import com.damocles.fleet.fleetmanagementsystembackend.dto.user.UserCreateDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.user.UserUpdateDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.user.UserResponseDTO;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface IUserMapper {

    // --- CREATE -> ENTITY ---
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "driver", ignore = true)
    })
    User toEntity(UserCreateDTO dto);

    // --- READ -> DTO (record) ---
    // accountId -> Long
    @Mappings({
            @Mapping(target = "accountId",
                    expression = "java(u.getAccount() != null ? u.getAccount().getId() : null)")
    })
    UserResponseDTO toResponse(User u);


    // --- PARTIAL UPDATE ---
    @Mappings({
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "driver", ignore = true),
            @Mapping(target = "id", ignore = true)
    })
    void update(@MappingTarget User entity, UserUpdateDTO dto);

}