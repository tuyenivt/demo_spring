package com.example.versioning.metrics;

import java.time.Instant;

public record VersionUsageSnapshot(
        long requests,
        Instant lastUsed
) {
}
