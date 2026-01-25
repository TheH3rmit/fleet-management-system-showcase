package com.damocles.fleet.fleetmanagementsystembackend.repository;

import com.damocles.fleet.fleetmanagementsystembackend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    boolean existsByEmailIgnoreCase(String email);
    Optional<User> findByEmailIgnoreCase(String email);

    // Full fetch (roles + account + driver)
    @EntityGraph(attributePaths = {"account", "driver"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> fetchGraph(@Param("id") Long id);

    // Paged search
    @Query("""
           SELECT u FROM User u
           WHERE (:q IS NULL OR
                LOWER(COALESCE(u.firstName,  '')) LIKE CONCAT('%', LOWER(CAST(:q AS string)), '%') OR
                LOWER(COALESCE(u.middleName, '')) LIKE CONCAT('%', LOWER(CAST(:q AS string)), '%') OR
                LOWER(COALESCE(u.lastName,   '')) LIKE CONCAT('%', LOWER(CAST(:q AS string)), '%') OR
                LOWER(COALESCE(u.email,      '')) LIKE CONCAT('%', LOWER(CAST(:q AS string)), '%'))
           """)
    Page<User> search(@Param("q") String query, Pageable pageable);

    // Light list search (autocomplete)
    @Query("""
        select u from User u
        where lower(u.firstName) like concat('%', :q, '%')
           or lower(u.lastName) like concat('%', :q, '%')
           or lower(u.email) like concat('%', :q, '%')
        order by u.lastName asc, u.firstName asc
    """)
    List<User> searchUsers(@Param("q") String q, Pageable pageable);
}
