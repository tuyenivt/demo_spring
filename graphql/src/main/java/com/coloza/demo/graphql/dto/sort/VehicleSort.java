package com.coloza.demo.graphql.dto.sort;

public record VehicleSort(
        VehicleSortField field,
        SortDirection direction
) {
    public SortDirection getDirectionOrDefault() {
        return direction != null ? direction : SortDirection.ASC;
    }
}
