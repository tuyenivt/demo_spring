package com.coloza.demo.graphql.dto.filter;

public record VehicleFilter(
        VehicleTypeFilter type,
        UUIDFilter studentId,
        DateTimeFilter createdAt
) {
}
