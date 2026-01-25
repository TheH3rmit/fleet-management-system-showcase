package com.damocles.fleet.fleetmanagementsystembackend.service;
import com.damocles.fleet.fleetmanagementsystembackend.dto.loginHistory.LoginHistoryDTO;

import java.util.List;
public interface ILoginHistoryService {
    List<LoginHistoryDTO> getAllHistories();
    List<LoginHistoryDTO> getHistoriesByAccount(Long accountId);
    LoginHistoryDTO getHistoryById(Long id);
    void logAttempt(Long accountId, String result, String ip, String userAgent);
}
