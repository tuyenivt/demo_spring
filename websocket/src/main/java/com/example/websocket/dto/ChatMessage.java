package com.example.websocket.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Incoming chat message from client.
 * <p>
 * Uses Java 21 record for immutability and conciseness.
 * Jackson handles JSON serialization/deserialization.
 */
public record ChatMessage(
        String username,
        String content
) {
    /**
     * Explicit constructor for Jackson deserialization.
     * Ensures proper field mapping from JSON.
     */
    @JsonCreator
    public ChatMessage(
            @JsonProperty("username") String username,
            @JsonProperty("content") String content
    ) {
        this.username = username;
        this.content = content;
    }
}
