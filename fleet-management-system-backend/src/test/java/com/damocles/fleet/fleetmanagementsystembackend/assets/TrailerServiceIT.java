package com.damocles.fleet.fleetmanagementsystembackend.assets;

import com.damocles.fleet.fleetmanagementsystembackend.domain.TrailerStatus;
import com.damocles.fleet.fleetmanagementsystembackend.dto.trailer.CreateTrailerRequest;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITransportRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITrailerRepository;
import com.damocles.fleet.fleetmanagementsystembackend.service.TrailerService;
import com.damocles.fleet.fleetmanagementsystembackend.support.AbstractPostgresIT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TrailerServiceIT extends AbstractPostgresIT {

    @Autowired TrailerService service;
    @Autowired ITrailerRepository trailerRepository;
    @Autowired ITransportRepository transportRepository;

    @AfterEach
    void clean() {
        transportRepository.deleteAll();
        trailerRepository.deleteAll();
    }

    @Test
    void create_search_and_update_status() {
        var created = service.createTrailer(new CreateTrailerRequest(
                "Trailer",
                "TR-1000",
                new BigDecimal("1000"),
                new BigDecimal("10")
        ));

        var page = service.searchTrailers("tr-1000", PageRequest.of(0, 10));
        assertFalse(page.isEmpty());

        var updated = service.updateStatus(created.id(), TrailerStatus.IN_SERVICE);
        assertEquals(TrailerStatus.IN_SERVICE, updated.trailerStatus());
    }
}
