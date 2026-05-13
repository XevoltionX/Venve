package com.cs.venue.controller;

import com.cs.common.dto.ApiResponse;
import com.cs.venue.entity.Venue;
import com.cs.venue.service.VenueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    public ApiResponse<List<Venue>> list(@RequestParam(required = false) String location) {
        return ApiResponse.ok(venueService.listAvailable(location));
    }

    @GetMapping("/{id}")
    public ApiResponse<Venue> getById(@PathVariable Long id) {
        return ApiResponse.ok(venueService.getById(id));
    }
}
