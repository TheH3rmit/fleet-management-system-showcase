package com.damocles.fleet.fleetmanagementsystembackend.service;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Account;
import com.damocles.fleet.fleetmanagementsystembackend.domain.LoginHistory;
import com.damocles.fleet.fleetmanagementsystembackend.dto.loginHistory.LoginHistoryDTO;
import com.damocles.fleet.fleetmanagementsystembackend.exception.LoginHistoryNotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.NotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.ILoginHistoryMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ILoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginHistoryService implements ILoginHistoryService {

    private final ILoginHistoryRepository loginHistoryRepository;
    private final IAccountRepository accountRepository;
    private final ILoginHistoryMapper mapper;

    @Override
    // Returns all login history entries.
    public List<LoginHistoryDTO> getAllHistories() {
        return loginHistoryRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    // Lists login histories for a given account.
    public List<LoginHistoryDTO> getHistoriesByAccount(Long accountId) {
        return loginHistoryRepository.findByAccount_IdOrderByLoggedAtDesc(accountId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    // Fetches a single login history entry by id.
    public LoginHistoryDTO getHistoryById(Long id) {
        LoginHistory h = loginHistoryRepository.findById(id)
                .orElseThrow(() -> new LoginHistoryNotFoundException(id));
        return mapper.toDto(h);
    }

    @Override
    // Records a login attempt with basic context details.
    public void logAttempt(Long accountId, String result, String ip, String userAgent) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        LoginHistory history = LoginHistory.builder()
                .account(account)
                .loggedAt(Instant.now())
                .ip(ip)
                .userAgent(userAgent)
                .result(result)
                .build();

        loginHistoryRepository.save(history);
    }
}
