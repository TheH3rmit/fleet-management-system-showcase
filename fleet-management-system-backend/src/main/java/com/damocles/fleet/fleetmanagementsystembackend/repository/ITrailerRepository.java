package com.damocles.fleet.fleetmanagementsystembackend.repository;


import com.damocles.fleet.fleetmanagementsystembackend.domain.Trailer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ITrailerRepository extends JpaRepository<Trailer, Long> {

    @Query("""
    SELECT t
    FROM Trailer t
    WHERE t.trailerStatus = com.damocles.fleet.fleetmanagementsystembackend.domain.TrailerStatus.ACTIVE
      AND NOT EXISTS (
          SELECT tr FROM Transport tr
          WHERE tr.trailer = t
            AND tr.status IN (
                com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.PLANNED,
                com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.ACCEPTED,
                com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.IN_PROGRESS
            )
      )
""")
    List<Trailer> findAvailable();

    @Query("""
        SELECT t FROM Trailer t
        WHERE (
            :q IS NULL
            OR LOWER(COALESCE(t.name, '')) LIKE CONCAT('%', :q, '%')
            OR LOWER(COALESCE(t.licensePlate, '')) LIKE CONCAT('%', :q, '%')
            OR LOWER(STR(t.trailerStatus)) LIKE CONCAT('%', :q, '%')
            OR STR(t.id) LIKE CONCAT('%', :q, '%')
        )
    """)
    Page<Trailer> search(@Param("q") String q, Pageable pageable);
}
