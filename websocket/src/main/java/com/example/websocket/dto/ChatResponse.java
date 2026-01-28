package com.example.websocket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

/**
 * Outgoing chat response to clients.
 * <p>
 * Includes server-side timestamp and message type.
 */
public record ChatResponse(
        String username,
        String content,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant timestamp,

        String messageType
) {
}
