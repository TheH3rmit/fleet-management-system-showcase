package com.damocles.fleet.fleetmanagementsystembackend.service;

import com.damocles.fleet.fleetmanagementsystembackend.dto.admin.AdminCreateUserWithAccountDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.admin.AdminCreateUserWithAccountResponseDTO;
import jakarta.validation.Valid;

public interface IAdminService {

    // Creates a new user with account credentials in a single transaction.
    AdminCreateUserWithAccountResponseDTO createUserWithAccount(@Valid AdminCreateUserWithAccountDTO dto);
}
