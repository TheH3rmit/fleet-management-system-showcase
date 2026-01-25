package com.damocles.fleet.fleetmanagementsystembackend.repository;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Transport;
import com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ITransportRepository extends JpaRepository<Transport, Long>, JpaSpecificationExecutor<Transport> {

    // basic existence checks
    boolean existsByDriver_UserId(Long driverId);
    boolean existsByVehicle_Id(Long vehicleId);
    boolean existsByTrailer_Id(Long trailerId);

    boolean existsByPickupLocation_Id(Long locationId);
    boolean existsByDeliveryLocation_Id(Long locationId);

    // status-scoped existence checks
    boolean existsByDriver_UserIdAndStatusAndIdNot(Long driverId, TransportStatus status, Long id);
    boolean existsByVehicle_IdAndStatus(Long vehicleId, TransportStatus status);
    boolean existsByTrailer_IdAndStatus(Long trailerId, TransportStatus status);
    boolean existsByVehicle_IdAndStatusIn(Long vehicleId, List<TransportStatus> statuses);
    boolean existsByTrailer_IdAndStatusIn(Long trailerId, List<TransportStatus> statuses);

    @Query("""
        SELECT COUNT(t) > 0
        FROM Transport t
        WHERE t.vehicle.id = :vehicleId
          AND t.status IN (
              com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.PLANNED,
              com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.ACCEPTED,
              com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.IN_PROGRESS
          )
    """)
    boolean existsActiveTransportByVehicle(@Param("vehicleId") Long vehicleId);

    @Query("""
        SELECT COUNT(t) > 0
        FROM Transport t
        WHERE t.driver.userId = :driverId
          AND t.status IN (
              com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.PLANNED,
              com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.ACCEPTED,
              com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.IN_PROGRESS
          )
    """)
    boolean existsActiveTransportByDriver(@Param("driverId") Long driverId);

    @Query("""
        SELECT t FROM Transport t
        WHERE t.driver.userId = :driverId
        ORDER BY t.plannedStartAt DESC
    """)
    List<Transport> findByDriver(@Param("driverId") Long driverId);

    @Query("""
        SELECT COUNT(t) > 0
        FROM Transport t
        WHERE t.trailer.id = :trailerId
          AND t.status IN (
              com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.PLANNED,
              com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.ACCEPTED,
              com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus.IN_PROGRESS
          )
    """)
    boolean existsActiveTransportByTrailer(@Param("trailerId") Long trailerId);
}
