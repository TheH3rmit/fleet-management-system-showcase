package com.damocles.fleet.fleetmanagementsystembackend.repository;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ILocationRepository extends JpaRepository<Location, Long> {
    boolean existsByStreetAndBuildingNumberAndCity(String street, String buildingNumber, String city);

    @Query("""
        SELECT l
        FROM Location l
        WHERE (
            :q IS NULL
            OR LOWER(COALESCE(l.city, '')) LIKE CONCAT('%', :q, '%')
            OR LOWER(COALESCE(l.street, '')) LIKE CONCAT('%', :q, '%')
            OR LOWER(COALESCE(l.postcode, '')) LIKE CONCAT('%', :q, '%')
            OR LOWER(COALESCE(l.country, '')) LIKE CONCAT('%', :q, '%')
            OR LOWER(COALESCE(l.buildingNumber, '')) LIKE CONCAT('%', :q, '%')
            OR STR(l.id) LIKE CONCAT('%', :q, '%')
        )
    """)
    Page<Location> searchPage(@Param("q") String q, Pageable pageable);

    @Query("""
    SELECT l
    FROM Location l
    WHERE LOWER(l.city) LIKE LOWER(CONCAT('%', :q, '%'))
       OR LOWER(l.street) LIKE LOWER(CONCAT('%', :q, '%'))
       OR LOWER(l.postcode) LIKE LOWER(CONCAT('%', :q, '%'))
""")
    List<Location> search(@Param("q") String keyword);
}
