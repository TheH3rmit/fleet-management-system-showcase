package com.damocles.fleet.fleetmanagementsystembackend.support;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Transport;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.CreateTransportRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.TransportDTO;
import com.damocles.fleet.fleetmanagementsystembackend.dto.transport.TransportDetailsDTO;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.ITransportMapper;

public class TestTransportMapper implements ITransportMapper {
    @Override
    public TransportDTO toDto(Transport transport) {
        return null;
    }

    @Override
    public Transport toEntity(CreateTransportRequest req) {
        return new Transport();
    }

    @Override
    public void updateFromDto(CreateTransportRequest req, Transport transport) {
        // no-op for tests that only verify validation paths
    }

    @Override
    public TransportDetailsDTO toDetailsDTO(Transport t) {
        return null;
    }
}
