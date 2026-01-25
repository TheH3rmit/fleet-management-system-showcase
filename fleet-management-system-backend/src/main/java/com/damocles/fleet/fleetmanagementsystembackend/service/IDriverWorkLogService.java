package com.damocles.fleet.fleetmanagementsystembackend.service;

import com.damocles.fleet.fleetmanagementsystembackend.dto.driverWorkLog.CreateDriverWorkLogRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.driverWorkLog.DriverWorkLogDTO;

import java.util.List;

public interface IDriverWorkLogService {
    List<DriverWorkLogDTO> getAllLogs();
    DriverWorkLogDTO getLogById(Long id);
    List<DriverWorkLogDTO> getLogsByDriver(Long driverId);
    List<DriverWorkLogDTO> getLogsByTransport(Long transportId);
    DriverWorkLogDTO createLog(CreateDriverWorkLogRequest req);
    DriverWorkLogDTO updateLog(Long id, CreateDriverWorkLogRequest req);
    void deleteLog(Long id);
}
