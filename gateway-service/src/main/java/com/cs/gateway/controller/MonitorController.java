package com.cs.gateway.controller;

import com.cs.gateway.metrics.MetricsCollector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private final MetricsCollector collector;

    public MonitorController(MetricsCollector collector) {
        this.collector = collector;
    }

    @GetMapping
    public Map<String, Object> metrics() {
        Map<String, MetricsCollector.RouteMetric> routes = collector.snapshot();
        long totalQps = routes.values().stream().mapToLong(m -> m.qps).sum();
        long totalErrors = routes.values().stream().mapToLong(m -> m.errors).sum();

        return Map.of(
                "totalQps", totalQps,
                "totalErrors", totalErrors,
                "routes", routes,
                "timestamp", System.currentTimeMillis()
        );
    }
}
