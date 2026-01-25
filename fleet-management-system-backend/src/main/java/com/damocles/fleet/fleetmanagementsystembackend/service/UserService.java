package com.damocles.fleet.fleetmanagementsystembackend.service;

import com.damocles.fleet.fleetmanagementsystembackend.domain.User;
import com.damocles.fleet.fleetmanagementsystembackend.dto.user.UserCreateDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.user.UserResponseDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.user.UserUpdateDTO;
import com.damocles.fleet.fleetmanagementsystembackend.exception.*;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.IUserMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IDriverWorkLogRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IDriverRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITransportRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IUserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements IUserService {

    private final IUserRepository IUserRepository;
    private final IAccountRepository accountRepository;
    private final IDriverRepository driverRepository;
    private final ITransportRepository transportRepository;
    private final IDriverWorkLogRepository DriverWorkLogRepository;
    private final IUserMapper mapper;


    @Override
    // Creates a new user with email validation.
    public UserResponseDTO create(@Valid UserCreateDTO dto) {
        String email = normalize(dto.email());
        if (email == null || email.isBlank()) {
            throw new BusinessValidationException("Email is required");
        }
        if (IUserRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        User u = mapper.toEntity(dto);
        u.setEmail(email);
        return mapper.toResponse(IUserRepository.save(u));
    }

    @Override
    // Fetches a user by id with optional graph loading.
    public UserResponseDTO get(Long id, boolean withGraph) {
        User u = (withGraph ? IUserRepository.fetchGraph(id) : IUserRepository.findById(id))
                .orElseThrow(() -> new UserNotFoundException(id));
        return mapper.toResponse(u);
    }

    @Override
    // Lists users using search and pagination.
    public Page<UserResponseDTO> list(String q, Pageable pageable) {
        return IUserRepository.search(emptyToNull(q), pageable)
                .map(mapper::toResponse);
    }

    @Override
    // Updates user data with email uniqueness checks.
    public UserResponseDTO update(Long id, @Valid UserUpdateDTO dto) {
        User u = IUserRepository.fetchGraph(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // validation and email change
        if (dto.email() != null) {
            var newEmail = normalize(dto.email());
            if (newEmail != null && !newEmail.equalsIgnoreCase(u.getEmail())
                    && IUserRepository.existsByEmailIgnoreCase(newEmail)) {
                throw new EmailAlreadyUsedException(newEmail);
            }
        }

        mapper.update(u, dto);
        if (dto.email() != null) {
            var newEmail = normalize(dto.email());
            if (newEmail != null) {
                u.setEmail(newEmail);
            }
        }

        return mapper.toResponse(u);
    }

    @Override
    // Deletes a user and related account/driver if safe.
    public void delete(Long id) {
        User user = IUserRepository.fetchGraph(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (user.getDriver() != null) {
            Long driverId = user.getDriver().getUserId();

            if (transportRepository.existsByDriver_UserId(driverId)) {
                throw new BusinessValidationException("Driver has transports and cannot be deleted");
            }

            if (DriverWorkLogRepository.existsByDriver_UserId(driverId)) {
                throw new BusinessValidationException("Driver has work logs and cannot be deleted");
            }

            driverRepository.delete(user.getDriver());
        }

        if (user.getAccount() != null) {
            accountRepository.delete(user.getAccount());
        }

        IUserRepository.delete(user);
    }

    // --- helpers ---


    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    // Searches users by query with a bounded size limit.
    public List<UserResponseDTO> search(String q, int size) {
        String query = q == null ? "" : q.trim();
        if (query.length() < 2) return List.of();

        int limit = Math.min(Math.max(size, 1), 20);

        return IUserRepository
                .searchUsers(query.toLowerCase(), PageRequest.of(0, limit))
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}

