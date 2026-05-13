package com.cs.venue.repository;

import com.cs.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long>, JpaSpecificationExecutor<Venue> {

    List<Venue> findByStatus(Venue.VenueStatus status);

    List<Venue> findByStatusAndLocationStartingWith(Venue.VenueStatus status, String location);
}
