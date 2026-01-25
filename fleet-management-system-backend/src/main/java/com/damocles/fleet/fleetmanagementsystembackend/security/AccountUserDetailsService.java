package com.damocles.fleet.fleetmanagementsystembackend.security;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Account;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountUserDetailsService implements UserDetailsService {

    private final IAccountRepository accountRepo;

    @Override
    // Loads account credentials and authorities for Spring Security authentication.
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepo.findWithUserByLoginIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found: " + username));

        if (account.getStatus() == null || account.getStatus() != com.damocles.fleet.fleetmanagementsystembackend.domain.AccountStatus.ACTIVE) {
            throw new org.springframework.security.authentication.DisabledException("Account is not ACTIVE");
        }

        Set<GrantedAuthority> authorities = account.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
                account.getLogin(),
                account.getPasswordHash(),
                authorities
        );
    }
}
