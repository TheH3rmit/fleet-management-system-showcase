package com.damocles.fleet.fleetmanagementsystembackend.dto.admin;

import com.damocles.fleet.fleetmanagementsystembackend.dto.account.AccountResponseDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.user.UserResponseDTO;

public record AdminCreateUserWithAccountResponseDTO(
        UserResponseDTO user,
        AccountResponseDTO account
) {}