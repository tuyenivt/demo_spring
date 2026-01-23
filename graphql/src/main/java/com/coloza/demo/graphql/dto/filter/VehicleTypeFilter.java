package com.coloza.demo.graphql.dto.filter;

import com.coloza.demo.graphql.enums.VehicleType;

import java.util.List;

public record VehicleTypeFilter(
        VehicleType eq,
        List<VehicleType> in
) {
}
