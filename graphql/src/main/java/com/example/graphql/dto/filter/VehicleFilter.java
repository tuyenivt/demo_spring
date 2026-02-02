package com.example.graphql.dto.filter;

public record VehicleFilter(
        VehicleTypeFilter type,
        UUIDFilter studentId,
        DateTimeFilter createdAt
) {
}
