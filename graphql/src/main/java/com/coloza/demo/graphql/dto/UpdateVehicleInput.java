package com.coloza.demo.graphql.dto;

import java.util.UUID;

public record UpdateVehicleInput(
        UUID id,
        String type,
        UUID studentId
) {
}
