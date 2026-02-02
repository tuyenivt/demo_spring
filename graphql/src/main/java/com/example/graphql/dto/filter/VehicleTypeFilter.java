package com.example.graphql.dto.filter;

import com.example.graphql.enums.VehicleType;

import java.util.List;

public record VehicleTypeFilter(
        VehicleType eq,
        List<VehicleType> in
) {
}
