package com.damocles.fleet.fleetmanagementsystembackend.service;

import com.damocles.fleet.fleetmanagementsystembackend.dto.trailer.CreateTrailerRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.trailer.TrailerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ITrailerService{
    List<TrailerDTO> getAllTrailers();
    Page<TrailerDTO> searchTrailers(String q, Pageable pageable);
    TrailerDTO getTrailerById(Long id);
    TrailerDTO createTrailer(CreateTrailerRequest req);
    TrailerDTO updateTrailer(Long id, CreateTrailerRequest req);
    List<TrailerDTO> getAvailableTrailers();
    void deleteTrailer(Long id);
}
