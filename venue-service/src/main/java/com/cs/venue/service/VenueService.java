package com.cs.venue.service;

import com.cs.venue.entity.Venue;
import com.cs.venue.repository.VenueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public List<Venue> listAvailable(String location) {
        if (location != null && !location.isEmpty()) {
            return venueRepository.findByStatusAndLocationStartingWith(
                    Venue.VenueStatus.AVAILABLE, location);
        }
        return venueRepository.findByStatus(Venue.VenueStatus.AVAILABLE);
    }

    public Venue getById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("场馆不存在"));
    }
}
