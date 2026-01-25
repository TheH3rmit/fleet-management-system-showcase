package com.damocles.fleet.fleetmanagementsystembackend.repository;

import com.damocles.fleet.fleetmanagementsystembackend.domain.DriverWorkLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IDriverWorkLogRepository extends JpaRepository<DriverWorkLog, Long> {
    boolean existsByDriver_UserId(Long driverId);

    List<DriverWorkLog> findByDriver_UserId(Long driverId);
    List<DriverWorkLog> findByDriver_UserIdOrderByStartTimeDesc(Long driverId);

    List<DriverWorkLog> findByTransport_Id(Long transportId);
    List<DriverWorkLog> findByTransport_IdOrderByStartTimeDesc(Long transportId);
}

