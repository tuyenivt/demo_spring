package com.example.websocket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

/**
 * Error response sent to clients when message processing fails.
 */
public record ErrorResponse(
        String errorCode,
        String message,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant timestamp
) {
}
