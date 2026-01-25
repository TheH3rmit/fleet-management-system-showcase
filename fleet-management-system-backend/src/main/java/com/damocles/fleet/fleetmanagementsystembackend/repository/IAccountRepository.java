package com.damocles.fleet.fleetmanagementsystembackend.repository;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface IAccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    // basic
    boolean existsByLoginIgnoreCase(String login);
    Optional<Account> findByLoginIgnoreCase(String login);
    Optional<Account> findByUser_Id(Long userId);

    // full graph (account + user + role)
    @EntityGraph(attributePaths = { "user", "roles" })
    Optional<Account> findWithUserById(Long id);

    @EntityGraph(attributePaths = { "user", "roles" })
    Optional<Account> findWithUserByLoginIgnoreCase(String login);

}
