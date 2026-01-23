package com.coloza.demo.graphql.dto;

import com.coloza.demo.graphql.enums.VehicleType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateVehicleInput(
        @NotNull VehicleType type,
        UUID studentId
) {
}
