package com.damocles.fleet.fleetmanagementsystembackend.repository;

import com.damocles.fleet.fleetmanagementsystembackend.domain.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IStatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByTransport_IdOrderByChangedAtDesc(Long transportId);
}
