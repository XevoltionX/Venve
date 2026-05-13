package com.cs.gateway.metrics;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MetricsCollector {

    private final Map<String, RouteStats> routeStats = new ConcurrentHashMap<>();
    private volatile long lastSnapshotTime = System.currentTimeMillis();

    public void record(String route, long latencyMs, boolean error) {
        RouteStats stats = routeStats.computeIfAbsent(route, k -> new RouteStats());
        stats.totalRequests.incrementAndGet();
        stats.totalLatency.addAndGet(latencyMs);
        if (error) {
            stats.errorCount.incrementAndGet();
        }
    }

    public Map<String, RouteMetric> snapshot() {
        long now = System.currentTimeMillis();
        double intervalSec = (now - lastSnapshotTime) / 1000.0;
        if (intervalSec < 0.5) intervalSec = 1.0;
        lastSnapshotTime = now;

        Map<String, RouteMetric> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, RouteStats> e : routeStats.entrySet()) {
            RouteStats prev = e.getValue();
            long reqDelta = prev.totalRequests.getAndSet(0);
            long latDelta = prev.totalLatency.getAndSet(0);
            long errDelta = prev.errorCount.getAndSet(0);

            RouteMetric m = new RouteMetric();
            m.qps = Math.round(reqDelta / intervalSec);
            m.avgLatencyMs = reqDelta > 0 ? latDelta / reqDelta : 0;
            m.errors = errDelta;
            result.put(e.getKey(), m);
        }
        return result;
    }

    static class RouteStats {
        final AtomicLong totalRequests = new AtomicLong();
        final AtomicLong totalLatency = new AtomicLong();
        final AtomicLong errorCount = new AtomicLong();
    }

    public static class RouteMetric {
        public long qps;
        public long avgLatencyMs;
        public long errors;
    }
}
