package com.damocles.fleet.fleetmanagementsystembackend.repository;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Cargo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ICargoRepository extends JpaRepository<Cargo, Long> {
    List<Cargo> findAllByTransport_Id(Long transportId);
    List<Cargo> findAllByTransport_Driver_UserId(Long driverId);

    @Query("""
        SELECT c FROM Cargo c
        WHERE (
            :q IS NULL
            OR LOWER(COALESCE(c.cargoDescription, '')) LIKE CONCAT('%', :q, '%')
            OR STR(c.id) LIKE CONCAT('%', :q, '%')
            OR STR(c.transport.id) LIKE CONCAT('%', :q, '%')
            OR STR(c.weightKg) LIKE CONCAT('%', :q, '%')
            OR STR(c.volumeM3) LIKE CONCAT('%', :q, '%')
        )
    """)
    Page<Cargo> search(@Param("q") String q, Pageable pageable);
}
