package com.example.swagger.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standard error response")
public record ErrorResponse(
        @Schema(example = "2026-02-06T10:30:00Z")
        Instant timestamp,
        @Schema(example = "404")
        int status,
        @Schema(example = "Pet not found")
        String message
) {
}
