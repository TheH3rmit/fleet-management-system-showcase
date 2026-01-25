package com.damocles.fleet.fleetmanagementsystembackend.repository;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IDriverRepository extends JpaRepository<Driver, Long> {

    boolean existsByDriverLicenseNumber(String driverLicenseNumber);
    boolean existsByUserId(Long userId);

    Optional<Driver> findByUserId(Long userId);

    @Query("""
    SELECT d
    FROM Driver d
    WHERE d.driverStatus = com.damocles.fleet.fleetmanagementsystembackend.domain.DriverStatus.AVAILABLE
      AND NOT EXISTS (
            SELECT 1 FROM Transport t
            WHERE t.driver = d
              AND t.status IN (
                  com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.PLANNED,
                  com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.ACCEPTED,
                  com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.IN_PROGRESS
              )
      )
""")
    List<Driver> findAvailable();

    @Query("""
        SELECT d FROM Driver d
        JOIN d.user u
        WHERE (
            :q IS NULL
            OR LOWER(COALESCE(u.firstName, '')) LIKE CONCAT('%', :q, '%')
            OR LOWER(COALESCE(u.lastName, '')) LIKE CONCAT('%', :q, '%')
            OR LOWER(COALESCE(u.email, '')) LIKE CONCAT('%', :q, '%')
            OR LOWER(COALESCE(u.phone, '')) LIKE CONCAT('%', :q, '%')
            OR LOWER(COALESCE(d.driverLicenseNumber, '')) LIKE CONCAT('%', :q, '%')
            OR LOWER(COALESCE(d.driverLicenseCategory, '')) LIKE CONCAT('%', :q, '%')
            OR LOWER(STR(d.driverStatus)) LIKE CONCAT('%', :q, '%')
            OR STR(d.userId) LIKE CONCAT('%', :q, '%')
        )
    """)
    Page<Driver> search(@Param("q") String q, Pageable pageable);

}
