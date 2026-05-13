package com.cs.booking.feign;

import com.cs.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "venue-service")
public interface VenueFeignClient {

    @GetMapping("/api/venues/{id}")
    ApiResponse<Map<String, Object>> getVenue(@PathVariable Long id);

    @GetMapping("/api/venues")
    ApiResponse<List<Map<String, Object>>> listVenues();
}
