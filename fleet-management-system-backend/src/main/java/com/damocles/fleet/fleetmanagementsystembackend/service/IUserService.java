package com.damocles.fleet.fleetmanagementsystembackend.service;

import com.damocles.fleet.fleetmanagementsystembackend.dto.user.UserCreateDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.user.UserResponseDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.user.UserUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    UserResponseDTO create(UserCreateDTO dto);
    UserResponseDTO get(Long id, boolean withGraph); // with addition of relation
    Page<UserResponseDTO> list(String q, Pageable pageable);
    UserResponseDTO update(Long id, UserUpdateDTO dto);
    void delete(Long id);
}
