package com.example.graphql.dto.pagination;

public record PageInfoConnection(
        boolean hasNextPage,
        boolean hasPreviousPage,
        String startCursor,
        String endCursor
) {
}
