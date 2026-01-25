package com.damocles.fleet.fleetmanagementsystembackend.web;

import com.damocles.fleet.fleetmanagementsystembackend.dto.user.UserCreateDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.user.UserResponseDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.user.UserUpdateDTO;
import com.damocles.fleet.fleetmanagementsystembackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //user creation
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // Create a new user.
    public UserResponseDTO create(@Valid @RequestBody UserCreateDTO dto) {
        return userService.create(dto);
    }

    //getting selected user with id
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    @GetMapping("/{id}")
    // Get user by id.
    public UserResponseDTO get(@PathVariable Long id) {
        return userService.get(id, false);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    @GetMapping("/{id}/details")
    // Get user with graph details by id.
    public UserResponseDTO getDetails(@PathVariable Long id) {
        return userService.get(id, true);
    }

    //getting all users
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    @GetMapping
    // List users with optional search and pagination.
    public Page<UserResponseDTO> list(@RequestParam(required = false) String q,
                                      Pageable pageable) {
        return userService.list(q, pageable);
    }

    //updating partially selected user with id
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    // Partially update user by id.
    public UserResponseDTO update(@PathVariable Long id,
                                  @Valid @RequestBody UserUpdateDTO dto) {
        return userService.update(id, dto);
    }

    //deleting user with selected id
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // Delete user by id.
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

}
