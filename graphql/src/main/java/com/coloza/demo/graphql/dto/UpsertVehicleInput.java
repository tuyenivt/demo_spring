package com.coloza.demo.graphql.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UpsertVehicleInput(
        UUID id,
        @NotBlank String type,
        UUID studentId
) {
}
