package com.damocles.fleet.fleetmanagementsystembackend.dto.view;

import com.damocles.fleet.fleetmanagementsystembackend.dto.account.AccountResponseDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.user.UserResponseDTO;

public record AccountUserViewDTO(
        AccountResponseDTO account,
        UserResponseDTO user
) {}