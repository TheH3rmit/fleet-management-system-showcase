package com.damocles.fleet.fleetmanagementsystembackend.repository;
import com.damocles.fleet.fleetmanagementsystembackend.domain.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface ILoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findByAccount_Id(Long accountId);
    List<LoginHistory> findByAccount_IdOrderByLoggedAtDesc(Long accountId);
}
