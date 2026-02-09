package com.example.graphql.dto.input;

import com.example.graphql.enums.VehicleType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateVehicleInput(
        @NotNull VehicleType type,
        UUID studentId
) {
}
