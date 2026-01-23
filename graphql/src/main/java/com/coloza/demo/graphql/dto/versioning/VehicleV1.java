package com.coloza.demo.graphql.dto.versioning;

import com.coloza.demo.graphql.entity.Vehicle;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VehicleV1(
        UUID id,
        String type,
        StudentV1 student,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static VehicleV1 from(Vehicle vehicle) {
        return new VehicleV1(
                vehicle.getId(),
                vehicle.getType().name(),
                vehicle.getStudent() != null ? StudentV1.from(vehicle.getStudent()) : null,
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}
