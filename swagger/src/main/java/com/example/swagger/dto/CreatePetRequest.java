package com.example.swagger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for creating a pet")
public record CreatePetRequest(
        @Schema(description = "Pet name", example = "Buddy")
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be <= 100 chars")
        String name,
        @Schema(description = "Pet status", example = "available")
        @NotBlank(message = "status is required")
        @Pattern(regexp = "available|pending|sold", message = "status must be one of: available, pending, sold")
        String status
) {
}
