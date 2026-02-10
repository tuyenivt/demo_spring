package com.example.versioning.dto;

import java.time.Instant;
import java.util.List;

public record ReportV2(
        Long id,
        String title,
        String content,
        String author,
        Instant createdAt,
        List<String> tags
) {
}
