package com.damocles.fleet.fleetmanagementsystembackend.service;
import com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Trailer;
import com.damocles.fleet.fleetmanagementsystembackend.domain.TrailerStatus;
import com.damocles.fleet.fleetmanagementsystembackend.dto.trailer.CreateTrailerRequest;
import com.damocles.fleet.fleetmanagementsystembackend.dto.trailer.TrailerDTO;
import com.damocles.fleet.fleetmanagementsystembackend.exception.BusinessValidationException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.NotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.exception.TrailerNotFoundException;
import com.damocles.fleet.fleetmanagementsystembackend.mapper.ITrailerMapper;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITrailerRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITransportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TrailerService implements ITrailerService {

    private final ITrailerRepository trailerRepository;
    private final ITrailerMapper trailerMapper;
    private final ITransportRepository transportRepository;

    @Override
    // Returns all trailers with assignment flags.
    public List<TrailerDTO> getAllTrailers() {
        return trailerRepository.findAll()
                .stream()
                .map(this::enrichTrailer)
                .toList();
    }

    @Override
    // Searches trailers by free-text query with paging.
    public Page<TrailerDTO> searchTrailers(String q, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim().toLowerCase();

        if (query == null) {
            return trailerRepository.findAll(pageable).map(this::enrichTrailer);
        }

        return trailerRepository.search(query, pageable).map(this::enrichTrailer);
    }

    @Override
    // Fetches a trailer by id.
    public TrailerDTO getTrailerById(Long id) {
        Trailer trailer = trailerRepository.findById(id)
                .orElseThrow(() -> new TrailerNotFoundException(id));
        return enrichTrailer(trailer);
    }

    @Override
    // Creates a trailer record.
    public TrailerDTO createTrailer(CreateTrailerRequest req) {
        Trailer trailer = trailerMapper.toEntity(req);
        trailerRepository.save(trailer);
        return enrichTrailer(trailer);
    }

    @Override
    // Updates trailer fields by id.
    public TrailerDTO updateTrailer(Long id, CreateTrailerRequest req) {
        Trailer trailer = trailerRepository.findById(id)
                .orElseThrow(() -> new TrailerNotFoundException(id));
        trailerMapper.updateTrailerFromDto(req, trailer);
        return enrichTrailer(trailer);
    }

    @Override
    // Returns trailers that are currently available.
    public List<TrailerDTO> getAvailableTrailers() {
        return trailerRepository.findAvailable()
                .stream()
                .map(this::enrichTrailer)
                .toList();
    }

    @Override
    // Deletes a trailer only if it is not assigned to any transport.
    public void deleteTrailer(Long id) {
        Trailer trailer = trailerRepository.findById(id)
                .orElseThrow(() -> new TrailerNotFoundException(id));
        if (transportRepository.existsByTrailer_Id(id)) {
            throw new BusinessValidationException("Trailer is assigned to a transport and cannot be deleted");
        }
        trailerRepository.delete(trailer);
    }

    @Transactional
    // Updates trailer status when not assigned to active transport.
    public TrailerDTO updateStatus(Long id, TrailerStatus status) {
        Trailer t = trailerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Trailer not found"));

        if (transportRepository.existsByTrailer_IdAndStatusIn(
                id,
                List.of(TransportStatus.ACCEPTED, TransportStatus.IN_PROGRESS)
        )) {
            throw new BusinessValidationException("Trailer is assigned to an active transport and status cannot be changed");
        }

        t.setTrailerStatus(status);
        return enrichTrailer(t);
    }

    private TrailerDTO enrichTrailer(Trailer t) {
        TrailerDTO base = trailerMapper.toDto(t);
        boolean assigned = transportRepository.existsByTrailer_Id(t.getId());
        boolean inProgress = transportRepository.existsByTrailer_IdAndStatusIn(
                t.getId(),
                List.of(TransportStatus.ACCEPTED, TransportStatus.IN_PROGRESS)
        );
        return new TrailerDTO(
                base.id(),
                base.name(),
                base.licensePlate(),
                base.payload(),
                base.volume(),
                base.trailerStatus(),
                assigned,
                inProgress
        );
    }
}
