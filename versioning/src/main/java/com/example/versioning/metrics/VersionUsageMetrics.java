package com.example.versioning.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class VersionUsageMetrics {

    private final MeterRegistry meterRegistry;
    private final Map<String, VersionUsageSnapshot> usageByVersion = new ConcurrentHashMap<>();

    public void record(String version, String method, String status) {
        Counter.builder("api.version.requests")
                .tag("api.version", version)
                .tag("method", method)
                .tag("status", status)
                .register(meterRegistry)
                .increment();

        usageByVersion.compute(version, (k, existing) -> {
            if (existing == null) {
                return new VersionUsageSnapshot(1L, Instant.now());
            }
            return new VersionUsageSnapshot(existing.requests() + 1, Instant.now());
        });
    }

    public Map<String, VersionUsageSnapshot> snapshots() {
        return Map.copyOf(usageByVersion);
    }
}
