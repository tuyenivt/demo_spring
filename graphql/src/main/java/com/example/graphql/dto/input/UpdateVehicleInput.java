package com.example.graphql.dto.input;

import com.example.graphql.enums.VehicleType;

import java.util.UUID;

public record UpdateVehicleInput(
        UUID id,
        VehicleType type,
        UUID studentId
) {
}
