package com.coloza.demo.graphql.dto;

import com.coloza.demo.graphql.enums.VehicleType;

import java.util.UUID;

public record UpdateVehicleInput(
        UUID id,
        VehicleType type,
        UUID studentId
) {
}
