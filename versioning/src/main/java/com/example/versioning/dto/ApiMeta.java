package com.example.versioning.dto;

import java.time.Instant;

public record ApiMeta(
        String apiVersion,
        String deprecation,
        Instant timestamp
) {
}
