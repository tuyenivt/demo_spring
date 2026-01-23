package com.coloza.demo.graphql.dto.sort;

public record StudentSort(
        StudentSortField field,
        SortDirection direction
) {
    public SortDirection getDirectionOrDefault() {
        return direction != null ? direction : SortDirection.ASC;
    }
}
