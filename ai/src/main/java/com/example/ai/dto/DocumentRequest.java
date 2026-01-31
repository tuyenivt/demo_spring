package com.example.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record DocumentRequest(
        @NotBlank(message = "Content cannot be blank")
        @Size(max = 10000, message = "Content cannot exceed 10000 characters")
        String content,
        Map<String, Object> metadata
) {
    public DocumentRequest {
        if (metadata == null) {
            metadata = Map.of();
        }
    }
}
