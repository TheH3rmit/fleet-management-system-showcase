package com.damocles.fleet.fleetmanagementsystembackend.repository;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IVehicleRepository extends JpaRepository<Vehicle, Long> {
    boolean existsByLicensePlate(String licensePlate);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    @Query("""
    SELECT v
    FROM Vehicle v
    WHERE v.vehicleStatus = com.damocles.fleet.fleetmanagementsystembackend.domain.VehicleStatus.ACTIVE
      AND NOT EXISTS (
            SELECT t FROM Transport t
            WHERE t.vehicle = v
              AND t.status IN (
                  com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.PLANNED,
                  com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.ACCEPTED,
                  com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.IN_PROGRESS
              )
      )
""")
    List<Vehicle> findAvailable();

    @Query("""
        SELECT v FROM Vehicle v
        WHERE (
            :q IS NULL
            OR LOWER(COALESCE(v.manufacturer, '')) LIKE CONCAT('%', :q, '%')
            OR LOWER(COALESCE(v.model, '')) LIKE CONCAT('%', :q, '%')
            OR LOWER(COALESCE(v.licensePlate, '')) LIKE CONCAT('%', :q, '%')
            OR LOWER(STR(v.vehicleStatus)) LIKE CONCAT('%', :q, '%')
            OR STR(v.id) LIKE CONCAT('%', :q, '%')
        )
    """)
    Page<Vehicle> search(@Param("q") String q, Pageable pageable);
}
