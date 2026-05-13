package com.cs.venue.controller;

import com.cs.common.constant.AuthConstant;
import com.cs.common.dto.ApiResponse;
import com.cs.venue.entity.Venue;
import com.cs.venue.repository.VenueRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/venues")
public class VenueAdminController {

    private final VenueRepository venueRepository;

    public VenueAdminController(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    private void checkAdmin(String role) {
        if (!"ADMIN".equals(role)) throw new RuntimeException("无管理员权限");
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String status,
            @RequestHeader(AuthConstant.HEADER_USER_ROLE) String role) {
        checkAdmin(role);

        Specification<Venue> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isEmpty())
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            if (location != null && !location.isEmpty())
                predicates.add(cb.like(root.get("location"), "%" + location + "%"));
            if (status != null && !status.isEmpty())
                predicates.add(cb.equal(root.get("status"), Venue.VenueStatus.valueOf(status)));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Venue> result = venueRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by("id").ascending()));

        return ApiResponse.ok(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "currentPage", result.getNumber()
        ));
    }

    @PostMapping
    public ApiResponse<Venue> create(@RequestBody Venue venue,
                                      @RequestHeader(AuthConstant.HEADER_USER_ROLE) String role) {
        checkAdmin(role);
        venue.setId(null);
        venueRepository.save(venue);
        return ApiResponse.ok(venue);
    }

    @PutMapping("/{id}")
    public ApiResponse<Venue> update(@PathVariable Long id,
                                      @RequestBody Venue update,
                                      @RequestHeader(AuthConstant.HEADER_USER_ROLE) String role) {
        checkAdmin(role);
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("场馆不存在"));
        if (update.getName() != null) venue.setName(update.getName());
        if (update.getLocation() != null) venue.setLocation(update.getLocation());
        if (update.getDescription() != null) venue.setDescription(update.getDescription());
        if (update.getBusinessHours() != null) venue.setBusinessHours(update.getBusinessHours());
        if (update.getTableCount() != null) venue.setTableCount(update.getTableCount());
        if (update.getPricePerHour() != null) venue.setPricePerHour(update.getPricePerHour());
        if (update.getStatus() != null) venue.setStatus(update.getStatus());
        venueRepository.save(venue);
        return ApiResponse.ok(venue);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id,
                                       @RequestHeader(AuthConstant.HEADER_USER_ROLE) String role) {
        checkAdmin(role);
        venueRepository.deleteById(id);
        return ApiResponse.ok("ok");
    }
}
