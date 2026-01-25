package com.damocles.fleet.fleetmanagementsystembackend.service;

import com.damocles.fleet.fleetmanagementsystembackend.dto.account.AccountRolesUpdateDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.account.*;
import java.util.Optional;

public interface IAccountService {

    AccountResponseDTO register(AccountRegisterDTO dto);
    void changePassword(Long accountId, AccountChangePasswordDTO dto);
    void updateStatus(Long accountId, AccountStatusUpdateDTO dto);
    AccountResponseDTO getById(Long id);
    Optional<AccountResponseDTO> getByLogin(String login);
    void touchLastLogin(Long accountId);
    void updateRoles(Long accountId, AccountRolesUpdateDTO dto);
    void changePasswordAdmin(Long accountId, AccountAdminResetPasswordDTO dto);
}