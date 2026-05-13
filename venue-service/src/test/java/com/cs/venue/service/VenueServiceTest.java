package com.cs.venue.service;

import com.cs.venue.entity.Venue;
import com.cs.venue.repository.VenueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private VenueService venueService;

    @Test
    void shouldReturnAvailableVenuesOnly() {
        Venue v1 = new Venue();
        v1.setId(1L); v1.setName("1号台"); v1.setStatus(Venue.VenueStatus.AVAILABLE);
        Venue v2 = new Venue();
        v2.setId(5L); v2.setName("5号台"); v2.setStatus(Venue.VenueStatus.MAINTENANCE);

        when(venueRepository.findByStatus(Venue.VenueStatus.AVAILABLE))
                .thenReturn(List.of(v1));

        List<Venue> result = venueService.listAvailable(null);

        assertEquals(1, result.size());
        assertEquals("1号台", result.get(0).getName());
    }

    @Test
    void shouldFilterByLocation() {
        Venue v = new Venue();
        v.setId(1L); v.setName("1号台"); v.setLocation("A区");
        v.setStatus(Venue.VenueStatus.AVAILABLE);

        when(venueRepository.findByStatusAndLocationStartingWith(
                Venue.VenueStatus.AVAILABLE, "A区"))
                .thenReturn(List.of(v));

        List<Venue> result = venueService.listAvailable("A区");

        assertEquals(1, result.size());
        assertEquals("A区", result.get(0).getLocation());
    }

    @Test
    void shouldReturnEmptyWhenNoAvailable() {
        when(venueRepository.findByStatus(Venue.VenueStatus.AVAILABLE))
                .thenReturn(List.of());

        List<Venue> result = venueService.listAvailable(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetVenueById() {
        Venue v = new Venue();
        v.setId(1L); v.setName("1号台");
        when(venueRepository.findById(1L)).thenReturn(Optional.of(v));

        Venue result = venueService.getById(1L);
        assertEquals("1号台", result.getName());
    }

    @Test
    void shouldThrowWhenVenueNotFound() {
        when(venueRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> venueService.getById(999L));
    }
}
